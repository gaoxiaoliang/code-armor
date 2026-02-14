package com.code.armor.service;

import com.code.armor.entity.App;
import com.code.armor.entity.AppStatus;
import com.code.armor.entity.AppType;
import com.code.armor.repository.AppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppProcessingService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${firebase.token}")
    private String firebaseToken;

    @Value("${firebase.project}")
    private String firebaseProject;

    private final ObjectMapper objectMapper;

    private final AppRepository appRepository;

    @Async
    public void processApp(Long appId) {
        App app = appRepository.findById(appId).orElse(null);
        if (app == null) {
            log.warn("No app found with id {}", appId);
            return;
        }
        // check app type
        checkAppType(app);
        // build app
        buildApp(app);
    }

    private void unzip(Path source, Path target) {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(source))) {

            // -------- Step 1: 先读取所有 entry --------
            List<ZipEntry> entries = new ArrayList<>();
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(new ZipEntry(entry.getName()));
            }

            // 重新打开流
            try (ZipInputStream zis2 = new ZipInputStream(Files.newInputStream(source))) {

                // -------- Step 2: 计算公共根目录 --------
                String root = findCommonRoot(entries);

                // -------- Step 3: 正式解压 --------
                ZipEntry e;
                while ((e = zis2.getNextEntry()) != null) {

                    String name = e.getName();

                    // 去掉根目录
                    if (root != null && name.startsWith(root)) {
                        name = name.substring(root.length());
                    }

                    if (name.isEmpty()) continue;

                    Path newPath = target.resolve(name).normalize();

                    // 防止 Zip Slip
                    if (!newPath.startsWith(target)) {
                        throw new RuntimeException("Zip Slip attack detected");
                    }

                    if (e.isDirectory()) {
                        Files.createDirectories(newPath);
                    } else {
                        Files.createDirectories(newPath.getParent());
                        Files.copy(zis2, newPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String findCommonRoot(List<ZipEntry> entries) {
        String root = null;

        for (ZipEntry entry : entries) {
            String name = entry.getName();

            int idx = name.indexOf('/');
            if (idx <= 0) return null;

            String firstPart = name.substring(0, idx + 1);

            if (root == null) {
                root = firstPart;
            } else if (!root.equals(firstPart)) {
                return null;
            }
        }

        return root;
    }



    private int detectNodeVersion(Path packageJson) {
        JsonNode root = objectMapper.readTree(packageJson.toFile());
        JsonNode engines = root.path("engines");
        if (engines.has("node")) {
            String version = engines.get("node").asText();
            Pattern pattern = Pattern.compile("(\\d+)(?:\\.\\d+)*");
            Matcher matcher = pattern.matcher(version);

            int maxMajor = -1;
            while (matcher.find()) {
                String v = matcher.group(1);
                try {
                    int major = Integer.parseInt(v);
                    maxMajor = Math.max(maxMajor, major);
                } catch (NumberFormatException ignored) {}
            }
            return maxMajor;
        }

        return -1;
    }



    private void buildApp(App app) {
        if (!List.of(AppType.VUE, AppType.REACT, AppType.ANGULARJS)
                .contains(app.getAppType())) {
            return;
        }
        app.setStatus(AppStatus.COMPILING);
        appRepository.save(app);

        Path storagePath = Path.of(uploadDir, app.getStorageName());

        Path tempDir = null;
        try {
            String suffix = "-processing-" + UUID.randomUUID(); // 避免冲突
            tempDir = storagePath.toAbsolutePath().resolveSibling(storagePath.getFileName() + suffix);
            Files.createDirectory(tempDir);
        } catch (IOException e) {
            log.error("Error while creating temp directory", e);
            return;
        }

        unzip(storagePath, tempDir);

        Path packageJson = tempDir.resolve("package.json");
        if (!Files.exists(packageJson)) {
            throw new RuntimeException("package.json not found");
        }

        String nodeVersion = String.valueOf(detectNodeVersion(packageJson));

        runDockerInstall(tempDir, nodeVersion);

        runDockerBuild(tempDir, nodeVersion);
        app.setStatus(AppStatus.COMPILE_SUCCESS);
        appRepository.save(app);


        app.setStatus(AppStatus.DEPLOYING);
        appRepository.save(app);
        runCommand(tempDir.toFile(), List.of(
                "firebase", "hosting:sites:create", app.getStorageName(),
                "--token", firebaseToken,
                "--project", firebaseProject
        ));

        String buildOutput = findBuildOutput(tempDir);
        String firebaseJsonConf = """
                {
                   "hosting": {
                     "site": "%s",
                     "public": "%s",
                     "ignore": [
                       "firebase.json",
                       "**/.*",
                       "**/node_modules/**"
                     ],
                     "rewrites": [
                       {
                         "source": "**",
                         "destination": "/index.html"
                       }
                     ]
                   }
                 }
        """.formatted(app.getStorageName(), buildOutput);

        try {
            Files.writeString(tempDir.resolve("firebase.json"), firebaseJsonConf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        runCommand(tempDir.toFile(), List.of(
                "firebase", "deploy", "--only", "hosting",
                "--token", firebaseToken,
                "--project", firebaseProject
        ));
        app.setStatus(AppStatus.DEPLOY_SUCCESS);
        app.setSiteURL(String.format("https://%s.web.app/", app.getStorageName()));
        appRepository.save(app);
        deleteDirectory(tempDir);
    }

    private void deleteDirectory(Path dir) {
        if (!Files.exists(dir)) return;
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String findBuildOutput(Path projectDir) {
        Path dist = projectDir.resolve("dist");
        Path build = projectDir.resolve("build");
        if (Files.exists(dist)) return "dist";
        if (Files.exists(build)) return "build";
        throw new RuntimeException("No build output found");
    }


    private void runDockerBuild(Path projectDir, String nodeVersion) {
        List<String> command = List.of(
                "docker", "run", "--rm",
                "--cpus=2",
                "--memory=4g",
                "--pids-limit=100",
                "--read-only",
                "--network=none",
                "--security-opt=no-new-privileges",
                "--user", "node",
                "-v", projectDir + ":/app",
                "-w", "/app",
                "node:" + nodeVersion + "-alpine",
                "npm", "run", "build"
        );

        runCommand(null, command);
    }


    private void runDockerInstall(Path projectDir, String nodeVersion) {
        // 获取当前 Java 进程的 UID/GID
        String uid = executeAndGetOutput(List.of("id", "-u")).trim();
        String gid = executeAndGetOutput(List.of("id", "-g")).trim();

        List<String> command = List.of(
                "docker", "run", "--rm",
                "--cpus=2",
                "--memory=4g",
                "--pids-limit=100",
                "--security-opt=no-new-privileges",
                "--user", "root",      // ⭐ 使用宿主机用户
                "--network=bridge",
                "-v", projectDir.toAbsolutePath() + ":/app",
                "-w", "/app",
                "node:" + nodeVersion + "-alpine",
                "npm", "install"
        );

        runCommand(null, command);
    }

    /**
     * 运行命令并返回 stdout
     */
    private String executeAndGetOutput(List<String> command) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        try {
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            int code = process.waitFor();
            if (code != 0) {
                throw new RuntimeException("Command failed: " + command + ", exit code: " + code);
            }
            return sb.toString();
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }





    private void runCommand(File cwd, List<String> cmd) {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (cwd != null) {
            pb.directory(cwd);
        }
        pb.inheritIO();

        try {
            Process p = pb.start();
            int code = p.waitFor();
            if (code != 0)
                throw new RuntimeException(String.format("cmd = %s, code = %d", cmd, code));
        }  catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    private void checkAppType(App app) {
        app.setAppType(AppType.DETECTING);
        appRepository.save(app);

        Path tempDir;
        try {
            tempDir = Files.createTempDirectory("code-armor-app-processing-");
        } catch (IOException e) {
            log.error("Failed to create temp directory", e);
            return;
        }

        Path storagePath = Path.of(uploadDir, app.getStorageName());
        File tmpPackageJsonFile = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(storagePath.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith("package.json")) {
                    tmpPackageJsonFile = tempDir.resolve("package.json").toFile();
                    try (FileOutputStream fos = new FileOutputStream(tmpPackageJsonFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            log.error("Failed to extract package.json", e);
            return;
        }

        if (tmpPackageJsonFile == null) {
            app.setAppType(AppType.PURE_HTML_CSS_JS);
            appRepository.save(app);
            return;
        }

        JsonNode root = objectMapper.readTree(tmpPackageJsonFile);
        JsonNode dependencies = root.path("dependencies");
        if (dependencies == null) {
            app.setAppType(AppType.UNKNOWN);
            appRepository.save(app);
            return;
        }
        if (dependencies.has("vue")) {
            app.setAppType(AppType.VUE);
            appRepository.save(app);
            return;
        }
        if (dependencies.has("react")) {
            app.setAppType(AppType.REACT);
            appRepository.save(app);
            return;
        }
        if (dependencies.has("@angular/core")) {
            app.setAppType(AppType.ANGULARJS);
            appRepository.save(app);
            return;
        }
        app.setAppType(AppType.UNKNOWN);
        appRepository.save(app);
    }
}
