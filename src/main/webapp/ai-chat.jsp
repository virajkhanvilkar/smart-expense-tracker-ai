<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%
    /*
     * Check login
     */
    if (session.getAttribute("user") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>AI Financial Assistant</title>


    <!-- Bootstrap -->
    <link
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        rel="stylesheet">


    <style>

        body {
            background: #f5f7fb;
            font-family: Arial, sans-serif;
        }


        .chat-container {
            max-width: 900px;
            margin: 40px auto;
        }


        .chat-card {
            height: 650px;
            background: white;
            border-radius: 18px;
            box-shadow: 0 5px 25px rgba(0,0,0,0.08);
            overflow: hidden;
        }


        /* =========================
           HEADER
           ========================= */

        .chat-header {
            background: #212529;
            color: white;
            padding: 20px 25px;
        }


        .chat-header h4 {
            margin: 0;
            font-weight: 600;
        }


        .chat-header small {
            color: #ced4da;
        }


        /* =========================
           CHAT BODY
           ========================= */

        .chat-body {
            height: 480px;
            padding: 25px;
            overflow-y: auto;
            background: #f8f9fa;
        }


        .message {
            display: flex;
            margin-bottom: 18px;
        }


        .message.user {
            justify-content: flex-end;
        }


        .message.ai {
            justify-content: flex-start;
        }


        .message-content {
            max-width: 70%;
            padding: 12px 16px;
            border-radius: 15px;
            line-height: 1.5;
            white-space: pre-wrap;
        }


        .message.user .message-content {
            background: #0d6efd;
            color: white;
            border-bottom-right-radius: 3px;
        }


        .message.ai .message-content {
            background: white;
            color: #212529;
            border: 1px solid #dee2e6;
            border-bottom-left-radius: 3px;
        }


        /* =========================
           WELCOME MESSAGE
           ========================= */

        .welcome {
            text-align: center;
            margin-top: 100px;
            color: #6c757d;
        }


        .welcome-icon {
            font-size: 50px;
            margin-bottom: 15px;
        }


        /* =========================
           INPUT AREA
           ========================= */

        .chat-footer {
            padding: 15px 20px;
            background: white;
            border-top: 1px solid #dee2e6;
        }


        .input-group input {
            border-radius: 25px 0 0 25px;
            padding: 12px 18px;
        }


        .input-group button {
            border-radius: 0 25px 25px 0;
            padding: 12px 25px;
        }


        /* =========================
           TYPING
           ========================= */

        .typing {
            display: none;
            color: #6c757d;
            font-size: 14px;
            margin-bottom: 10px;
        }


        /* =========================
           QUICK QUESTIONS
           ========================= */

        .quick-question {
            border-radius: 20px;
            margin: 4px;
        }

    </style>

</head>


<body>


<div class="container chat-container">

    <div class="chat-card">


<!-- =========================
     HEADER
     ========================= -->

<div class="chat-header">

    <div class="d-flex justify-content-between align-items-center">

        <div>

            <h4>
                🤖 AI Financial Assistant
            </h4>

            <small>
                Ask questions about your expenses and income
            </small>

        </div>


        <!-- Back to Dashboard -->

        <a href="<%= request.getContextPath() %>/dashboard.jsp"
           class="btn btn-outline-light btn-sm">

            ← Dashboard

        </a>

    </div>

</div>

        <!-- =========================
             CHAT BODY
             ========================= -->

        <div
            id="chatBody"
            class="chat-body"
        >


            <div
                id="welcomeMessage"
                class="welcome"
            >

                <div class="welcome-icon">
                    🤖
                </div>

                <h5>
                    Hi! I'm your Financial Assistant
                </h5>

                <p>
                    I can analyze your transactions
                    and help you understand your spending.
                </p>


                <!-- QUICK QUESTIONS -->

                <div class="mt-4">

                    <button
                        class="btn btn-outline-primary btn-sm quick-question"
                        onclick="askQuickQuestion('How much did I spend?')"
                    >
                        💰 How much did I spend?
                    </button>


                    <button
                        class="btn btn-outline-success btn-sm quick-question"
                        onclick="askQuickQuestion('How much income do I have?')"
                    >
                        📈 What is my income?
                    </button>


                    <button
                        class="btn btn-outline-dark btn-sm quick-question"
                        onclick="askQuickQuestion('Give me my financial summary')"
                    >
                        📊 Financial summary
                    </button>


                    <button
                        class="btn btn-outline-warning btn-sm quick-question"
                        onclick="askQuickQuestion('How many transactions do I have?')"
                    >
                        🧾 Transaction count
                    </button>

                </div>

            </div>


            <!-- TYPING -->

            <div
                id="typing"
                class="typing"
            >
                🤖 AI is analyzing your transactions...
            </div>

        </div>


        <!-- =========================
             INPUT AREA
             ========================= -->

        <div class="chat-footer">

            <div class="input-group">

                <input
                    type="text"
                    id="messageInput"
                    class="form-control"
                    placeholder="Ask about your finances..."
                    autocomplete="off"
                >

                <button
                    id="sendButton"
                    class="btn btn-primary"
                    onclick="sendMessage()"
                >
                    Send
                </button>

            </div>


            <div class="text-center mt-2">

                <small class="text-muted">
                    Your questions are answered using your
                    transaction data.
                </small>

            </div>

        </div>

    </div>

</div>



<script>

    /*
     * ============================================================
     * CONTEXT PATH
     * ============================================================
     */

    const contextPath =
        "<%= request.getContextPath() %>";


    /*
     * ============================================================
     * SEND MESSAGE
     * ============================================================
     */

    function sendMessage() {

        const input =
            document.getElementById(
                "messageInput"
            );

        const message =
            input.value.trim();


        /*
         * Don't send empty message
         */

        if (message === "") {

            return;
        }


        /*
         * Remove welcome message
         */

        const welcome =
            document.getElementById(
                "welcomeMessage"
            );

        if (welcome) {

            welcome.remove();
        }


        /*
         * Display user message
         */

        addMessage(
            message,
            "user"
        );


        /*
         * Clear input
         */

        input.value = "";


        /*
         * Show typing
         */

        document.getElementById(
            "typing"
        ).style.display = "block";


        /*
         * Disable button
         */

        document.getElementById(
            "sendButton"
        ).disabled = true;


        /*
         * Send request to servlet
         */

        fetch(
            contextPath + "/aiChat",
            {

                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body:
                    "message="
                    + encodeURIComponent(message)

            }
        )

        .then(
            response => {

                if (!response.ok) {

                    throw new Error(
                        "Server error: "
                        + response.status
                    );
                }

                return response.text();
            }
        )

        .then(
            answer => {

                /*
                 * Hide typing
                 */

                document.getElementById(
                    "typing"
                ).style.display = "none";


                /*
                 * Display AI response
                 */

                addMessage(
                    answer,
                    "ai"
                );

            }
        )

        .catch(
            error => {

                console.error(
                    error
                );


                document.getElementById(
                    "typing"
                ).style.display = "none";


                addMessage(
                    "Sorry, something went wrong while connecting to the AI assistant.",
                    "ai"
                );
            }
        )

        .finally(
            () => {

                document.getElementById(
                    "sendButton"
                ).disabled = false;

                input.focus();

            }
        );
    }


    /*
     * ============================================================
     * ADD MESSAGE
     * ============================================================
     */

    function addMessage(
        message,
        sender
    ) {

        const chatBody =
            document.getElementById(
                "chatBody"
            );


        const messageDiv =
            document.createElement(
                "div"
            );


        messageDiv.className =
            "message " + sender;


        const content =
            document.createElement(
                "div"
            );


        content.className =
            "message-content";


        content.textContent =
            message;


        messageDiv.appendChild(
            content
        );


        chatBody.appendChild(
            messageDiv
        );


        /*
         * Scroll to bottom
         */

        chatBody.scrollTop =
            chatBody.scrollHeight;
    }


    /*
     * ============================================================
     * QUICK QUESTION
     * ============================================================
     */

    function askQuickQuestion(
        question
    ) {

        const input =
            document.getElementById(
                "messageInput"
            );


        input.value =
            question;


        sendMessage();
    }


    /*
     * ============================================================
     * ENTER KEY
     * ============================================================
     */

    document.getElementById(
        "messageInput"
    ).addEventListener(
        "keydown",
        function(event) {

            if (event.key === "Enter") {

                event.preventDefault();

                sendMessage();
            }

        }
    );

</script>


</body>

</html>