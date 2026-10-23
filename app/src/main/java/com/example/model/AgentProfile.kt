package com.example.model

data class AgentProfile(
    val id: String,
    val name: String,
    val category: AgentCategory,
    val roleTitle: String,
    val roleDescription: String,
    val systemInstruction: String,
    val defaultTemperature: Float = 0.3f,
    val isBuiltIn: Boolean = true
) {
    companion object {
        fun defaultAgents(): List<AgentProfile> = listOf(
            AgentProfile(
                id = "coder_senior",
                name = "Senior Programmer",
                category = AgentCategory.TECHNICAL,
                roleTitle = "Lead Software Engineer",
                roleDescription = "Deep architecture, Kotlin/Android code, battery optimization, algorithms & best practices.",
                systemInstruction = "Aap ek Senior Programmer hain. Code aur technical guidance dein. Provide clear explanations, clean code blocks, production best practices, and structured step-by-step solutions in Hindi/English/Hinglish to match the user's inquiry.",
                defaultTemperature = 0.3f,
                isBuiltIn = true
            ),
            AgentProfile(
                id = "research_analyst",
                name = "Research Analyst",
                category = AgentCategory.RESEARCH,
                roleTitle = "Senior Research Specialist",
                roleDescription = "Rigorous factual synthesis, comparative trade-offs, structured methodologies & citations.",
                systemInstruction = "Aap ek Research Analyst hain. Facts aur detailed explanation dein. Break down complex topics with structured bullet points, key pros & cons, historical/scientific context, and unbiased factual analysis.",
                defaultTemperature = 0.2f,
                isBuiltIn = true
            ),
            AgentProfile(
                id = "casual_assistant",
                name = "Friendly Assistant",
                category = AgentCategory.CASUAL,
                roleTitle = "Conversational AI Guide",
                roleDescription = "Warm, helpful dialogue, everyday questions, lifestyle tips & quick answers.",
                systemInstruction = "Aap ek Friendly Assistant hain. Warm, conversational, engaging aur helpful responses dein. Be cheerful, empathetic, clear, and approachable.",
                defaultTemperature = 0.7f,
                isBuiltIn = true
            ),
            AgentProfile(
                id = "creative_writer",
                name = "Creative Visionary",
                category = AgentCategory.CREATIVE,
                roleTitle = "Storyteller & Copywriter",
                roleDescription = "Compelling storytelling, poetic verses, brainstorming, metaphors & narratives.",
                systemInstruction = "Aap ek Creative Writer hain. Engaging stories, hooks, poetry aur creative ideas generate karein with vivid imagery and emotional resonance.",
                defaultTemperature = 0.8f,
                isBuiltIn = true
            )
        )

        const val DEFAULT_ROUTER_INSTRUCTION =
            "Aap ek Router Agent hain. User query ko analyze karke sirf category batayein: [TECHNICAL, RESEARCH, CASUAL, CREATIVE]. Return exactly the chosen category name along with a one-sentence rationale explaining why that agent is best suited."

        const val DEFAULT_BRAIN_INSTRUCTION = """Aap J.A.R.V.I.S ke Central Decision-Making Brain hain.
Aapka kaam input ko analyze karna, problem solve karna aur action steps decide karna hai.
Hamesha is structured format me response dein:
[THOUGHT PROCESS]: Problem ka breakdown.
[DECISION]: Kya action perform karna hai.
[TASK SEQUENCE]: Step-by-step execution list."""
    }
}
