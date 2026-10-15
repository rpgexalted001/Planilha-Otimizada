package com.example.rules

import java.io.File
import java.security.MessageDigest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Protege contra downgrade ou troca silenciosa de componentes do Gradle Wrapper. */
class GradleWrapperVersionContractTest {
    private val root: File by lazy {
        var current = File(System.getProperty("user.dir") ?: ".").absoluteFile
        repeat(7) {
            if (File(current, "settings.gradle.kts").isFile) return@lazy current
            current = current.parentFile ?: return@repeat
        }
        error("Raiz do projeto não encontrada.")
    }

    @Test
    fun `distribuicao Gradle 981 esta fixada e autenticada`() {
        val props = File(root, "gradle/wrapper/gradle-wrapper.properties").readText()
        assertTrue(props.contains("gradle-9.8.1-bin.zip"))
        assertTrue(props.contains("validateDistributionUrl=true"))
        assertTrue(props.contains("distributionSha256Sum=dce76f55f8e251a3a1f130eb120f30b3d271de2b76c9b0729d316b5a1b6dc01f"))
        assertTrue(File(root, "gradlew").isFile)
        assertTrue(File(root, "gradlew.bat").isFile)
    }

    @Test
    fun `wrapper jar corresponde ao checksum oficial do Gradle 981`() {
        val jar = File(root, "gradle/wrapper/gradle-wrapper.jar")
        assertTrue(jar.isFile)
        val sha256 = MessageDigest.getInstance("SHA-256").digest(jar.readBytes())
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        assertEquals("3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98", sha256)
    }

    @Test
    fun `build workflows usam wrapper local em vez de Gradle global`() {
        val paths = listOf(
            ".github/workflows/build_apk.yml",
            ".github/workflows/ci-auto-repair.yml",
            ".github/workflows/pr-regression-tests.yml",
            ".github/workflows/codeql-security-audit.yml"
        )
        paths.forEach { path ->
            val text = File(root, path).readText()
            assertTrue("$path precisa de ./gradlew", text.contains("./gradlew "))
            assertTrue("$path precisa da validação oficial do wrapper", text.contains("gradle/actions/setup-gradle@"))
        }
    }
}
