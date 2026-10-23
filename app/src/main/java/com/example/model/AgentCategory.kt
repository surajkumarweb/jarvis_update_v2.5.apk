package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberCreative
import com.example.ui.theme.CasualColor
import com.example.ui.theme.CyanPrimaryLight
import com.example.ui.theme.PurpleSpecialistLight

enum class AgentCategory(
    val title: String,
    val shortName: String,
    val description: String,
    val color: Color
) {
    TECHNICAL(
        title = "Technical / Coder",
        shortName = "TECHNICAL",
        description = "Senior Programmer providing code, architecture & technical guidance",
        color = CyanPrimaryLight
    ),
    RESEARCH(
        title = "Research Analyst",
        shortName = "RESEARCH",
        description = "In-depth research, facts, comparative analysis & explanations",
        color = PurpleSpecialistLight
    ),
    CASUAL(
        title = "Friendly Assistant",
        shortName = "CASUAL",
        description = "Warm conversational companion for everyday queries & discussions",
        color = CasualColor
    ),
    CREATIVE(
        title = "Creative Writer",
        shortName = "CREATIVE",
        description = "Storyteller, scriptwriter & imaginative ideation specialist",
        color = AmberCreative
    );

    companion object {
        fun fromString(value: String): AgentCategory {
            val upper = value.uppercase()
            return when {
                upper.contains("TECH") -> TECHNICAL
                upper.contains("RESEARCH") -> RESEARCH
                upper.contains("CREATIVE") -> CREATIVE
                upper.contains("CASUAL") -> CASUAL
                else -> TECHNICAL
            }
        }
    }
}
