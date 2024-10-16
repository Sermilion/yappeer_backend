import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Test {
    @Test
    fun testRoot() = testApplication {
        assertEquals(1, 1)
    }
}
