package com.neogpt.client.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownTest {

    @Test
    fun `plain paragraph is a single block`() {
        val blocks = Markdown.parse("Hello there")
        assertEquals(1, blocks.size)
        val paragraph = blocks.first() as MarkdownBlock.Paragraph
        assertEquals("Hello there", (paragraph.spans.single() as InlineSpan.Text).text)
    }

    @Test
    fun `blank lines separate paragraphs`() {
        val blocks = Markdown.parse("first\n\nsecond")
        assertEquals(2, blocks.size)
    }

    @Test
    fun `headings keep their level`() {
        val blocks = Markdown.parse("# Title\n\n### Smaller")
        assertEquals(2, blocks.size)
        assertEquals(1, (blocks[0] as MarkdownBlock.Heading).level)
        assertEquals(3, (blocks[1] as MarkdownBlock.Heading).level)
    }

    @Test
    fun `fenced code is captured verbatim with language`() {
        val source = "Intro\n\n```kotlin\nval x = 1\n\nprintln(x)\n```\n\nDone"
        val blocks = Markdown.parse(source)
        val code = blocks.filterIsInstance<MarkdownBlock.Code>().single()
        assertEquals("kotlin", code.language)
        assertEquals("val x = 1\n\nprintln(x)", code.code)
        assertEquals(3, blocks.size)
    }

    @Test
    fun `unordered and ordered lists are distinct`() {
        val unordered = Markdown.parse("- one\n- two")
        val unorderedList = unordered.single() as MarkdownBlock.ListBlock
        assertEquals(false, unorderedList.ordered)
        assertEquals(2, unorderedList.items.size)

        val ordered = Markdown.parse("1. first\n2. second")
        val orderedList = ordered.single() as MarkdownBlock.ListBlock
        assertTrue(orderedList.ordered)
        assertEquals(2, orderedList.items.size)
    }

    @Test
    fun `quote lines are merged`() {
        val blocks = Markdown.parse("> one\n> two")
        val quote = blocks.single() as MarkdownBlock.Quote
        assertEquals("one\ntwo", (quote.spans.single() as InlineSpan.Text).text)
    }

    @Test
    fun `inline bold italic and code nest`() {
        val spans = Markdown.parseInline("a **b *c* d** e")
        assertEquals(InlineSpan.Text("a "), spans[0])
        val bold = spans[1] as InlineSpan.Bold
        assertTrue(bold.children.contains(InlineSpan.Italic(listOf(InlineSpan.Text("c")))))
        assertEquals(InlineSpan.Text(" e"), spans[2])

        val code = Markdown.parseInline("use `a + b` now")
        assertTrue(code.contains(InlineSpan.Code("a + b")))
    }

    @Test
    fun `unclosed markers stay literal`() {
        val spans = Markdown.parseInline("2 * 3 ** 4")
        val text = spans.joinToString("") {
            when (it) {
                is InlineSpan.Text -> it.text
                else -> ""
            }
        }
        assertTrue(text.contains("*"))
    }

    @Test
    fun `safe links are parsed and unsafe ones degrade to text`() {
        val spans = Markdown.parseInline("[docs](https://example.com/a)")
        val link = spans.single() as InlineSpan.Link
        assertEquals("docs", link.text)
        assertEquals("https://example.com/a", link.url)

        val unsafe = Markdown.parseInline("[x](javascript:alert(1))")
        assertEquals(InlineSpan.Text("x"), unsafe.first())
        assertTrue(unsafe.none { it is InlineSpan.Link })
    }

    @Test
    fun `plainText strips markup`() {
        val blocks = Markdown.parse("# Hi\n\n**bold** text")
        val text = Markdown.plainText(blocks)
        assertTrue(text.contains("Hi"))
        assertTrue(text.contains("bold text"))
    }
}
