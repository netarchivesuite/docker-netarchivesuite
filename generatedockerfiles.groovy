#!/bin/groovy
import groovy.transform.CompileStatic
import groovy.transform.Field

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Field String RESSOURCE_DIR="resources";
@Field String DOCKER_FILEDIR="docker-files";
@Field String TEMPLATE_DIR="templates";
@Field String SUPERVISOR_DIR="templates/supervisor";
@Field String DOCKER_FILE="Dockerfile";

downloadResources()
new File(DOCKER_FILEDIR).deleteDir();

//createDockerFile new Debian(name: "ubuntu"), new JavaFlavor(flavor: "oracle", version: "8");
//createDockerFile new Debian(name: "ubuntu", version: "14.11"), new JavaFlavor(flavor: "oracle", version: "8");
//createDockerFile new Debian(name: "ubuntu", version: "14.11"), new JavaFlavor(flavor: "oracle", version: "7");
createDockerFile new Debian(name: "ubuntu", version: "14.04"), new JavaFlavor(flavor: "oracle", version: "6");

//createDockerFile new Debian(name: "centos", version: "6"), new JavaFlavor(flavor: "oracle", version: "6");
//createDockerFile new Debian(name: "centos"), new JavaFlavor(flavor: "oracle", version: "6");

@CompileStatic
public void createDockerFile(LinuxFlavor linuxFlavor, JavaFlavor javaFlavor,
        Maintainer maintainer= new Maintainer(name: "Mikis Seth Sørensen", url: "https://github.com/mikisseth")) {
    def dockerFileDirPath = "$DOCKER_FILEDIR/$linuxFlavor-$javaFlavor"
    def Path dockerFileDir = Paths.get DOCKER_FILEDIR, "$linuxFlavor-$javaFlavor";
    Files.createDirectories(dockerFileDir)

    def dockerFile = new File(dockerFileDirPath, DOCKER_FILE)
    dockerFile.write("# Docker file for running a NetarchiveSuite Quickstart system.\n")
    dockerFile.append("FROM ${linuxFlavor.toStringSemicolon()}\n")
    dockerFile.append("MAINTAINER $maintainer\n", 'UTF-8')

    // Copy supervisor files
    new File(SUPERVISOR_DIR).eachFile { File file ->
        Files.copy(Paths.get(file.getPath()), Paths.get(dockerFileDirPath, file.name),
                StandardCopyOption.REPLACE_EXISTING)
    }

    def fileLocations = [
            "$TEMPLATE_DIR/linuxflavor/setup-${linuxFlavor.template}.docker",
            "$TEMPLATE_DIR/setup-supervisor.docker",
            "$TEMPLATE_DIR/setup-test-user.docker",
            "$TEMPLATE_DIR/javaflavor/setup-${javaFlavor}.docker",
            "$TEMPLATE_DIR/setup-mq.docker",
            "$TEMPLATE_DIR/setup-netarchivesuite.docker"]
    fileLocations.each{ dockerFile.append(new File((String)it).getText()) }

    def buildFile = new File(dockerFileDir.toFile(), "build.sh")
    buildFile.write("#! /bin/bash\n\n")
    buildFile.append("docker build -t netarchivesuite/$linuxFlavor-$javaFlavor .")
    buildFile.setExecutable(true, true)

    // Link resource files
    Files.createDirectories(Paths.get(dockerFileDirPath, RESSOURCE_DIR))
    new File(RESSOURCE_DIR).eachFile { File file ->
        Files.createLink(Paths.get(dockerFileDirPath, RESSOURCE_DIR, file.name), Paths.get(RESSOURCE_DIR, file.name))
    }
}

private void downloadResources() {
    def Path resourceDir = Paths.get RESSOURCE_DIR;
    Files.createDirectories(resourceDir)
    downloadResource("https://sbforge.org/svn/netarchivesuite/trunk/scripts/openmq/mq.sh");
    downloadResource("http://download.java.net/mq/open-mq/4.5.2/latest/openmq4_5_2-binary-Linux_X86.zip");
    downloadResource("https://sbforge.org/jenkins/job/NetarchiveSuite/lastSuccessfulBuild/artifact/deploy/releasezipball/target/NetarchiveSuite-5.0-SNAPSHOT.zip");
    downloadResource("https://raw.githubusercontent.com/netarchivesuite/netarchivesuite/master/deploy/deploy/scripts/RunNetarchiveSuite.sh");
    downloadResource("https://raw.githubusercontent.com/netarchivesuite/netarchivesuite/master/unclassified/examples/deploy_standalone_example.xml");
    downloadResource("https://sbforge.org/svn/netarchivesuite/trunk/scripts/openmq/mq.sh")
}

private def downloadResource(String url) {
    println "Downloading $url"
    executeOnShell("wget -N -nv $url", new File(RESSOURCE_DIR))
    return
}

private def executeOnShell(String command, File workingDir) {
    def process = new ProcessBuilder(["sh","-c",command]).directory(workingDir).redirectErrorStream(true).start()
    process.inputStream.eachLine {println it}
}

abstract class LinuxFlavor {
    String name="ubuntu";
    String version="";

    abstract String install();
    abstract String getTemplate();

    String toString() {
        return "$name" + (version!=""?"-$version":"");
    }

    String toStringSemicolon() {
        return "$name" + (version!=""?":$version":"");
    }
}

class Redhat extends LinuxFlavor {
    @Override
    String install() {
        return null
    }

    @Override
    String getTemplate() {
        return 'redhat'
    }
}
class Debian extends LinuxFlavor {
    @Override
    String install() {
        return null
    }
    @Override
    String getTemplate() {
        return 'debian'
    }
}

class JavaFlavor {
    String flavor;
    String version;

    String toString() {
        return "$flavor-jdk$version";
    }
}

class Maintainer {
    String name;
    String url="";

    String toString() {
        return "$name \"$url\"";
    }
}