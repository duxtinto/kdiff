package com.duxtinto.kdiff

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KDiffParserTest {
    @Test
    internal fun `parse an empty diff should return empty list`() {
        // Arrange
        val sut = KDiffParser()

        // Act
        val diffs = sut.parse("")

        // Arrange
        assertThat(diffs).isEmpty()
    }

    @Test
    internal fun `parse a diff header`() {
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
    internal fun `parse a single-line diff hunk header`() {
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
    internal fun `parse a multi-line diff hunk`() {
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
                            .containsEntry(1, "public class Main {")
                            .containsEntry(2, "   public static void main(String[] args) {")
                            .containsEntry(3, "       // old line")
                            .containsEntry(6, "   }")
                            .containsEntry(7, "}")
                }
            }

            this.to.apply {
                assertThat(this.start).isEqualTo(3)
                assertThat(this.count).isEqualTo(6)
                this.lines.apply {
                    assertThat(this)
                            .hasSize(6)
                            .containsEntry(1, "public class Main {")
                            .containsEntry(2, "   public static void main(String[] args) {")
                            .containsEntry(4, "       // new line")
                            .containsEntry(5, "       // another new line")
                            .containsEntry(6, "   }")
                            .containsEntry(7, "}")
                }
            }
        }
    }

    @Test
    @Tag("testing")
    internal fun `parse a multi-hunk diff`() {
        // Arrange
        val sut = KDiffParser()
        val rawDiff = """
            diff --git a/Main.java b/Main.java
            --- a/Main.java
            +++ b/Main.java
            @@ -3,3 +3,3 @@
             public class Main {
                public static void main(String[] args) {
            -       // old line
            +       // new line
            @@ -50,3 +50,3 @@
            -       // another old line
            +       // another new line
                }
             }
        """.trimIndent()

        // Act
        val diffs = sut.parse(rawDiff)

        // Arrange
        assertThat(diffs).hasSize(1)
        val diff = diffs.first()
        assertThat(diff.hunks).hasSize(2)
        with(diff.hunks.first()) {
            this.from.apply {
                assertThat(this.start).isEqualTo(3)
                assertThat(this.count).isEqualTo(3)
                this.lines.apply {
                    assertThat(this)
                            .hasSize(3)
                            .containsEntry(1, "public class Main {")
                            .containsEntry(2, "   public static void main(String[] args) {")
                            .containsEntry(3, "       // old line")
                }
            }

            this.to.apply {
                assertThat(this.start).isEqualTo(3)
                assertThat(this.count).isEqualTo(3)
                this.lines.apply {
                    assertThat(this)
                            .hasSize(3)
                            .containsEntry(1, "public class Main {")
                            .containsEntry(2, "   public static void main(String[] args) {")
                            .containsEntry(4, "       // new line")
                }
            }
        }

        with(diff.hunks[1]) {
            this.from.apply {
                assertThat(this.start).isEqualTo(50)
                assertThat(this.count).isEqualTo(3)
                this.lines.apply {
                    assertThat(this)
                            .hasSize(3)
                            .containsEntry(6, "       // another old line")
                            .containsEntry(8, "   }")
                            .containsEntry(9, "}")
                }
            }

            this.to.apply {
                assertThat(this.start).isEqualTo(50)
                assertThat(this.count).isEqualTo(3)
                this.lines.apply {
                    assertThat(this)
                            .hasSize(3)
                            .containsEntry(7, "       // another new line")
                            .containsEntry(8, "   }")
                            .containsEntry(9, "}")
                }
            }
        }
    }
}
