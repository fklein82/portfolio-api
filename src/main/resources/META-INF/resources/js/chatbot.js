// Central Chatbot functionality
(function() {
    const chatInput = document.getElementById('chat-input-main');
    const chatSend = document.getElementById('chat-send-main');
    const chatMessages = document.getElementById('chatbot-messages-main');

    // Send message on button click
    chatSend.addEventListener('click', sendMessage);

    // Send message on Enter key
    chatInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    async function sendMessage() {
        const message = chatInput.value.trim();
        if (!message) return;

        // Disable input while processing
        chatInput.disabled = true;
        chatSend.disabled = true;

        // Add user message to chat
        addMessage(message, 'user');
        chatInput.value = '';

        // Auto-scroll to bottom
        scrollToBottom();

        // Add typing indicator
        const typingIndicator = addTypingIndicator();

        try {
            // Send message to API and stream response
            const response = await fetch('/api/chat/stream', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    message: message,
                    role: 'user'
                })
            });

            if (!response.ok) {
                throw new Error('Erreur réseau: ' + response.status);
            }

            // Remove typing indicator
            typingIndicator.remove();

            // Create bot message element for streaming
            const botMessageDiv = document.createElement('div');
            botMessageDiv.className = 'chat-message bot-message';
            const botMessageP = document.createElement('p');
            botMessageDiv.appendChild(botMessageP);
            chatMessages.appendChild(botMessageDiv);
            scrollToBottom();

            // Read streaming response (SSE format)
            const reader = response.body.getReader();
            const decoder = new TextDecoder();
            let fullText = '';
            let buffer = '';

            while (true) {
                const { done, value } = await reader.read();
                if (done) break;

                // Decode chunk and add to buffer
                const chunk = decoder.decode(value, { stream: true });
                buffer += chunk;

                // Process complete SSE events (delimited by \n)
                let newlineIndex;
                while ((newlineIndex = buffer.indexOf('\n')) !== -1) {
                    let line = buffer.substring(0, newlineIndex);
                    buffer = buffer.substring(newlineIndex + 1);

                    // Remove potential \r at end of line (for \r\n line endings)
                    if (line.endsWith('\r')) {
                        line = line.substring(0, line.length - 1);
                    }

                    if (line.startsWith('data:')) {
                        // Extract content after "data:" (5 chars)
                        // Quarkus SSE puts the leading space of content after the colon
                        // e.g., content " suis" becomes line "data: suis"
                        // So we extract from position 5 to preserve all spaces
                        const content = line.substring(5);

                        if (content !== '[DONE]' && content !== '') {
                            fullText += content;
                            botMessageP.textContent = fullText;
                            scrollToBottom();
                        }
                    }
                }
            }

        } catch (error) {
            console.error('Erreur:', error);
            typingIndicator.remove();
            addMessage('Désolé, une erreur s\'est produite. Assurez-vous que les API keys sont configurées dans le fichier .env', 'bot');
        } finally {
            // Re-enable input
            chatInput.disabled = false;
            chatSend.disabled = false;
            chatInput.focus();
        }
    }

    function addMessage(text, role) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `chat-message ${role === 'user' ? 'user-message' : 'bot-message'}`;

        const messageP = document.createElement('p');
        messageP.textContent = text;
        messageDiv.appendChild(messageP);

        chatMessages.appendChild(messageDiv);
        scrollToBottom();
    }

    function addTypingIndicator() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'chat-message bot-message typing-indicator';
        typingDiv.innerHTML = '<div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>';
        chatMessages.appendChild(typingDiv);
        scrollToBottom();
        return typingDiv;
    }

    function scrollToBottom() {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Smooth scroll for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Header scroll effect
    const header = document.querySelector('.header');
    let lastScroll = 0;

    window.addEventListener('scroll', () => {
        const currentScroll = window.pageYOffset;

        if (currentScroll > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }

        lastScroll = currentScroll;
    });

})();
