package com.algorena.security;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Hidden // Hide from Swagger documentation
public class OAuth2RedirectController {

    /**
     * Used for OAuth2 login redirection. Displays a simple HTML page with the JWT token. Only used for testing purposes.
     * Would have never done this without AI, might not be too bad after all.
     *
     * @param token
     * @param response
     * @throws IOException
     */
    @GetMapping(value = "/oauth2/redirect", produces = MediaType.TEXT_HTML_VALUE)
    public void oauth2Redirect(@RequestParam String token, HttpServletResponse response) throws IOException {
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>OAuth2 Login Success</title>
                    <style>
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            min-height: 100vh;
                            margin: 0;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        }
                        .container {
                            background: white;
                            padding: 2rem;
                            border-radius: 10px;
                            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                            max-width: 600px;
                            width: 90%%;
                        }
                        h1 {
                            color: #333;
                            margin-top: 0;
                        }
                        .success-icon {
                            color: #4CAF50;
                            font-size: 48px;
                            text-align: center;
                            margin-bottom: 1rem;
                        }
                        .token-container {
                            background: #f5f5f5;
                            padding: 1rem;
                            border-radius: 5px;
                            word-break: break-all;
                            font-family: 'Courier New', monospace;
                            font-size: 12px;
                            margin: 1rem 0;
                            max-height: 200px;
                            overflow-y: auto;
                        }
                        .copy-btn {
                            background: #667eea;
                            color: white;
                            border: none;
                            padding: 10px 20px;
                            border-radius: 5px;
                            cursor: pointer;
                            font-size: 16px;
                            width: 100%%;
                            margin-top: 1rem;
                        }
                        .copy-btn:hover {
                            background: #5568d3;
                        }
                        .copy-btn:active {
                            background: #4451a8;
                        }
                        .success-msg {
                            color: #4CAF50;
                            display: none;
                            text-align: center;
                            margin-top: 0.5rem;
                            font-weight: bold;
                        }
                        .instructions {
                            background: #e3f2fd;
                            padding: 1rem;
                            border-radius: 5px;
                            border-left: 4px solid #2196F3;
                            margin-top: 1rem;
                        }
                        .instructions h3 {
                            margin-top: 0;
                            color: #1976D2;
                        }
                        .instructions ol {
                            margin: 0.5rem 0;
                            padding-left: 1.5rem;
                        }
                        .instructions li {
                            margin: 0.5rem 0;
                        }
                        a {
                            color: #667eea;
                            text-decoration: none;
                        }
                        a:hover {
                            text-decoration: underline;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success-icon">✓</div>
                        <h1>Authentication Successful!</h1>
                        <p>Your JWT token has been generated. Copy it below to use in Swagger UI:</p>
                
                        <div class="token-container" id="token">""" + token + """
                </div>
                
                                        <button class="copy-btn" onclick="copyToken()">📋 Copy Token</button>
                                        <div class="success-msg" id="successMsg">✓ Token copied to clipboard!</div>
                
                                        <div class="instructions">
                                            <h3>How to use this token in Swagger:</h3>
                                            <ol>
                                                <li>Go to <a href="/swagger-ui.html" target="_blank">Swagger UI</a></li>
                                                <li>Click the <strong>"Authorize"</strong> button (lock icon)</li>
                                                <li>Paste the token in the <strong>"bearer-jwt"</strong> field</li>
                                                <li>Click <strong>"Authorize"</strong> then <strong>"Close"</strong></li>
                                                <li>Now you can test authenticated endpoints!</li>
                                            </ol>
                                        </div>
                                    </div>
                
                                    <script>
                                        function copyToken() {
                                            const tokenText = document.getElementById('token').textContent;
                                            navigator.clipboard.writeText(tokenText).then(() => {
                                                const successMsg = document.getElementById('successMsg');
                                                successMsg.style.display = 'block';
                                                setTimeout(() => {
                                                    successMsg.style.display = 'none';
                                                }, 3000);
                                            }).catch(err => {
                                                alert('Failed to copy token. Please copy it manually.');
                                            });
                                        }
                                    </script>
                                </body>
                                </html>
                """;

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(html);
    }
}

