package com.duxtinto.kdiff

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KDiffParserTest {
    @Test
    internal fun `parsing empty diff should return empty list`() {
        // Arrange
        val sut = KDiffParser()

        // Act
        val diffs = sut.parse("")

        // Arrange
        assertThat(diffs).isEmpty()
    }

    @Test
    internal fun parseAFileModificationHeader() {
        // Arrange
        val sut = KDiffParser()
        val diff = """
            diff --git a/Main.java b/Main.java
            --- a/Main.java
            +++ b/Main.java
        """.trimIndent()

        // Act
        val diffs = sut.parse(diff)

        // Arrange
        assertThat(diffs).hasSize(1)
        assertThat(diffs.first().from).isEqualTo("Main.java")
        assertThat(diffs.first().to).isEqualTo("Main.java")
    }

    @Test
    internal fun `parsing a single-line diff hunk header`() {
        // Arrange
        val sut = KDiffParser()
        val diff = """
            diff --git a/Main.java b/Main.java
            --- a/Main.java
            +++ b/Main.java
            @@ -117 +117 @@
        """.trimIndent()

        // Act
        val diffs = sut.parse(diff)

        // Arrange
        assertThat(diffs).hasSize(1)
        assertThat(diffs.first().hunks).hasSize(1)
        with(diffs.first().hunks.first()) {
            this.from.apply {
                assertThat(this.start).isEqualTo(117)
                assertThat(this.count).isEqualTo(0)
            }

            this.to.apply {
                assertThat(this.start).isEqualTo(117)
                assertThat(this.count).isEqualTo(0)
            }
        }
    }

    @Test
    internal fun `parsing a multi-line diff hunk`() {
        // Arrange
        val sut = KDiffParser()
        val diff = """
            diff --git a/Main.java b/Main.java
            --- a/Main.java
            +++ b/Main.java
            @@ -3,5 +3,6 @@
             public class Main {
                public static void main(String[] args) {
            -       // old line
            +       // new line
            +       // another new line
                }
             }
        """.trimIndent()

        // Act
        val diffs = sut.parse(diff)

        // Arrange
        assertThat(diffs).hasSize(1)
        assertThat(diffs.first().hunks).hasSize(1)
        with(diffs.first().hunks.first()) {
            this.from.apply {
                assertThat(this.start).isEqualTo(3)
                assertThat(this.count).isEqualTo(5)
                this.lines.apply {
                    assertThat(this)
                            .hasSize(5)
                            .contains(
                                    "public class Main {",
                                    "   public static void main(String[] args) {",
                                    "       // old line",
                                    "   }",
                                    "}")
                }
            }

            this.to.apply {
                assertThat(this.start).isEqualTo(3)
                assertThat(this.count).isEqualTo(6)
                this.lines.apply {
                    assertThat(this)
                            .hasSize(6)
                            .contains(
                                    "public class Main {",
                                    "   public static void main(String[] args) {",
                                    "       // new line",
                                    "       // another new line",
                                    "   }",
                                    "}")
                }
            }
        }
    }
}
