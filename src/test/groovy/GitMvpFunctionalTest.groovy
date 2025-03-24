
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class GitMvpPluginFunctionalTest extends Specification {
    @TempDir File testProjectDir
    File buildFile

    def setup() {
        buildFile = new File(testProjectDir, 'build.gradle')
        buildFile << """
            plugins {
                id 'io.github.duckasteroid.git-mvp'
            }
        """
    }

    def "can successfully configure tag prefix through extension"() {
        buildFile << """
            gitVersion {
                tagPrefix = 'wibble'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments('explainVersion')
                .withPluginClasspath()
                .build()

        then:
        result.output.contains("Successfully resolved URL 'https://www.google.com/'")
        result.task(":explainVersion").outcome == SUCCESS
    }
}