package com.example

import com.example.model.AgentCategory
import com.example.model.AgentProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAgentCategoryParsing() {
        assertEquals(AgentCategory.TECHNICAL, AgentCategory.fromString("TECHNICAL"))
        assertEquals(AgentCategory.TECHNICAL, AgentCategory.fromString("[TECHNICAL]"))
        assertEquals(AgentCategory.RESEARCH, AgentCategory.fromString("RESEARCH"))
        assertEquals(AgentCategory.CASUAL, AgentCategory.fromString("CASUAL"))
        assertEquals(AgentCategory.CREATIVE, AgentCategory.fromString("CREATIVE"))
    }

    @Test
    fun testDefaultAgentsConfiguration() {
        val agents = AgentProfile.defaultAgents()
        assertTrue(agents.size >= 4)

        val coder = agents.find { it.category == AgentCategory.TECHNICAL }
        assertNotNull(coder)
        assertTrue(coder!!.systemInstruction.contains("Senior Programmer"))

        val researcher = agents.find { it.category == AgentCategory.RESEARCH }
        assertNotNull(researcher)
        assertTrue(researcher!!.systemInstruction.contains("Research Analyst"))
    }

    @Test
    fun testRouterInstructionDefault() {
        val instruction = AgentProfile.DEFAULT_ROUTER_INSTRUCTION
        assertTrue(instruction.contains("Router Agent"))
        assertTrue(instruction.contains("[TECHNICAL, RESEARCH, CASUAL, CREATIVE]"))
    }
}
