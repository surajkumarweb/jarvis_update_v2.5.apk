package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MarkdownOutputView(
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val parts = content.split("```")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block
                val lines = part.lines()
                val lang = lines.firstOrNull()?.trim()?.ifBlank { "code" } ?: "code"
                val code = lines.drop(1).joinToString("\n").ifBlank { part }
                Spacer(modifier = Modifier.height(6.dp))
                CodeBlockView(code = code, language = lang)
                Spacer(modifier = Modifier.height(6.dp))
            } else {
                // Regular markdown text
                val trimmed = part.trim()
                if (trimmed.isNotBlank()) {
                    trimmed.lines().forEach { line ->
                        when {
                            line.startsWith("### ") -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = line.removePrefix("### ").trim(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            line.startsWith("#### ") -> {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = line.removePrefix("#### ").trim(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            line.startsWith("* ") || line.startsWith("- ") -> {
                                Text(
                                    text = "• " + line.substring(2).trim(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            line.startsWith("> ") -> {
                                Text(
                                    text = line.removePrefix("> ").trim(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            line.isNotBlank() -> {
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
