const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Load CV data
const cvData = JSON.parse(fs.readFileSync(path.join(__dirname, 'data', 'cv.json'), 'utf-8'));

// Build context string from CV for the LLM
function buildCvContext() {
  const p = cvData.personalInfo;
  let ctx = `Name: ${p.name}\nTitle: ${p.title}\nCompany: ${p.company}\nLocation: ${p.location}\n\n`;
  ctx += `Summary: ${cvData.summary}\n\n`;

  ctx += 'Experience:\n';
  for (const exp of cvData.experience) {
    ctx += `- ${exp.title} at ${exp.company} (${exp.startDate} - ${exp.endDate}): ${exp.description}\n`;
  }

  ctx += '\nCertifications:\n';
  for (const cert of cvData.certifications) {
    ctx += `- ${cert.name} (${cert.issuer}, ${cert.date}): ${cert.description}\n`;
  }

  ctx += '\nTechnical Skills: ' + cvData.skills.technical.join(', ') + '\n';
  ctx += 'Soft Skills: ' + cvData.skills.soft.join(', ') + '\n';

  ctx += '\nLanguages:\n';
  for (const lang of cvData.languages) {
    ctx += `- ${lang.language}: ${lang.proficiency}\n`;
  }

  return ctx;
}

const cvContext = buildCvContext();

const SYSTEM_PROMPT = `You are an assistant representing Frederic Klein, Associate Principal Solutions Architect at Red Hat.
You answer questions professionally and concisely using information from his CV.

CV CONTEXT:
${cvContext}

Instructions:
- IMPORTANT: Always respond in the SAME LANGUAGE as the user's question (French if question is in French, English if in English)
- Use the context information to answer precisely
- If the question is about Frederic, respond in first person
- If the information is not in the context, say so honestly
- Keep responses concise (2-3 paragraphs maximum)`;

// Health check
app.get('/api/chat/health', (req, res) => {
  res.json({ status: 'ok' });
});

// Profile endpoint
app.get('/api/profile', (req, res) => {
  res.json(cvData);
});

// Chat streaming endpoint using OpenAI API
app.post('/api/chat/stream', async (req, res) => {
  const { message } = req.body;
  if (!message || !message.trim()) {
    res.status(400).json({ error: 'Empty message' });
    return;
  }

  const apiKey = process.env.OPENAI_API_KEY;
  if (!apiKey) {
    res.status(500).json({ error: 'OPENAI_API_KEY not configured' });
    return;
  }

  // Set SSE headers
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.flushHeaders();

  try {
    const response = await fetch('https://api.openai.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`,
      },
      body: JSON.stringify({
        model: process.env.OPENAI_MODEL || 'gpt-4o-mini',
        max_tokens: 4096,
        stream: true,
        messages: [
          { role: 'system', content: SYSTEM_PROMPT },
          { role: 'user', content: message },
        ],
      }),
    });

    if (!response.ok) {
      const errText = await response.text();
      console.error('OpenAI API error:', response.status, errText);
      res.write(`data:Error: API returned ${response.status}\n\n`);
      res.end();
      return;
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });

      let newlineIdx;
      while ((newlineIdx = buffer.indexOf('\n')) !== -1) {
        const line = buffer.substring(0, newlineIdx).trim();
        buffer = buffer.substring(newlineIdx + 1);

        if (!line.startsWith('data: ')) continue;
        const data = line.substring(6);
        if (data === '[DONE]') break;

        try {
          const parsed = JSON.parse(data);
          const content = parsed.choices?.[0]?.delta?.content;
          if (content) {
            res.write(`data:${content}\n\n`);
          }
        } catch {
          // skip unparseable lines
        }
      }
    }

    res.write('data:[DONE]\n\n');
    res.end();
  } catch (err) {
    console.error('Stream error:', err);
    res.write(`data:Error: ${err.message}\n\n`);
    res.end();
  }
});

app.listen(PORT, () => {
  console.log(`Portfolio API running on port ${PORT}`);
});
