/**
 * AI 表达式助手
 *
 * 功能：
 * - 多轮对话：生成、解释、优化、调试 Aviator 表达式
 * - 自动组装上下文（函数、变量、追踪样本）
 * - 支持 Markdown 渲染 AI 回复
 * - 支持显示 AI 思考过程
 *
 * 依赖：layui, engine-const.js, ace-expression-editor.js, marked.js
 */
var AiExpressionHelper = (function () {

    // 将 AI 回复文本渲染为 HTML（优先使用 marked，降级为纯文本转义）
    function renderMd(text) {
        if (!text) return '';
        if (typeof marked !== 'undefined') {
            return marked.parse(text, { breaks: true, gfm: true });
        }
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML.replace(/\n/g, '<br>');
    }

    function AiExpressionHelper(config) {
        this.executorId = config.executorId;
        this.expressionId = config.expressionId || null;
        this.editor = config.editor || null;
        this.conversationHistory = [];
        this.layerIndex = null;
        this.loading = false;
        this.lastReasoningContent = null;
        this.lastStartTime = null;
        this.lastExpression = null;
        this._chatHtml = '';
        this._initialized = false;
        this._lastWidth = '520px';
        this._lastHeight = '600px';
    }

    AiExpressionHelper.prototype.openDialog = function () {
        var self = this;

        var dialogHtml = this._buildDialogHtml();

        this.layerIndex = layer.open({
            type: 1,
            title: '<i class="layui-icon layui-icon-dialogue" style="margin-right:6px;"></i>表达式助手',
            area: [self._lastWidth, self._lastHeight],
            offset: 'r',
            anim: 'slideLeft',
            shadeClose: true,
            content: dialogHtml,
            success: function (layero) {
                self._bindEvents(layero);
                self._enableResize(layero);
                // 恢复缓存的聊天内容
                if (self._chatHtml) {
                    var chatArea = document.getElementById('ai-chat-area');
                    if (chatArea) {
                        chatArea.innerHTML = self._chatHtml;
                        chatArea.scrollTop = chatArea.scrollHeight;
                    }
                } else if (!self._initialized) {
                    // 首次打开显示欢迎提示
                    self._initialized = true;
                    self._appendMessage('assistant',
                        '你好！我是表达式助手，可以帮你：\n' +
                        '- **生成表达式** — 用自然语言描述逻辑，我来写代码\n' +
                        '- **解释表达式** — 粘贴表达式，我逐行解读含义\n' +
                        '- **优化表达式** — 检查问题并提出改进建议\n' +
                        '- **调试排错** — 帮你定位表达式报错原因\n\n' +
                        '请描述你的需求：',
                        null, new Date());
                }
            },
            cancel: function () {
                // 弹窗关闭前缓存聊天 HTML
                var chatArea = document.getElementById('ai-chat-area');
                if (chatArea) {
                    self._chatHtml = chatArea.innerHTML;
                }
            },
            end: function () {
                // shadeClose 等方式关闭时也缓存
                var chatArea = document.getElementById('ai-chat-area');
                if (chatArea) {
                    self._chatHtml = chatArea.innerHTML;
                }
            }
        });
    };

    AiExpressionHelper.prototype._buildDialogHtml = function () {
        return '<div class="ai-dialog-container" style="display:flex;flex-direction:column;height:100%;font-size:13px;">' +
            '<div id="ai-chat-area" style="flex:1;overflow-y:auto;padding:12px;"></div>' +
            '<div style="padding:10px 10px 0;display:flex;gap:8px;align-items:flex-start;">' +
            '  <textarea id="ai-input" class="layui-textarea" style="height:60px;resize:none;flex:1;" placeholder="描述你的需求，例如：帮我解释这个表达式 或 金额大于100且用户是VIP"></textarea>' +
            '</div>' +
            '<div style="padding:8px 10px 10px;display:flex;justify-content:space-between;">' +
            '  <button class="layui-btn layui-btn-sm layui-btn-primary" id="ai-clear-btn"><i class="layui-icon layui-icon-delete"></i> 清空对话</button>' +
            '  <button class="layui-btn layui-btn-sm" id="ai-send-btn"><i class="layui-icon layui-icon-release"></i> 发送</button>' +
            '</div>' +
            '</div>';
    };

    AiExpressionHelper.prototype._bindEvents = function (layero) {
        var self = this;

        layero.find('#ai-send-btn').on('click', function () {
            self._handleSend();
        });

        layero.find('#ai-input').on('keydown', function (e) {
            if (e.ctrlKey && e.keyCode === 13) {
                self._handleSend();
            }
        });

        layero.find('#ai-clear-btn').on('click', function () {
            self._clearConversation();
        });
    };

    AiExpressionHelper.prototype._enableResize = function (layero) {
        var self = this;
        var $layer = layero;

        // 左边缘（调宽度）+ 底边缘（调高度）
        var handles = [
            { edge: 'left',   cursor: 'ew-resize',   css: 'top:0;left:0;width:6px;height:100%;' },
            { edge: 'bottom', cursor: 'ns-resize',    css: 'bottom:0;left:0;width:100%;height:6px;' }
        ];

        $.each(handles, function (_, h) {
            var $handle = $('<div style="position:absolute;' + h.css + 'cursor:' + h.cursor + ';z-index:10;background:transparent;"></div>');
            $layer.append($handle);

            $handle.on('mousedown', function (e) {
                e.preventDefault();
                e.stopPropagation();
                var startX = e.clientX;
                var startY = e.clientY;
                var startW = $layer[0].offsetWidth;
                var startH = $layer[0].offsetHeight;
                var startLeft = $layer[0].offsetLeft;

                $(document).on('mousemove.aiResize', function (e) {
                    var dx = e.clientX - startX;
                    var dy = e.clientY - startY;
                    if (h.edge === 'left' || h.edge === 'corner') {
                        var newW = Math.max(400, startW - dx);
                        // 右边缘固定，调整 left + width
                        $layer.css({ width: newW + 'px', left: (startLeft + startW - newW) + 'px' });
                        self._lastWidth = newW + 'px';
                    }
                    if (h.edge === 'bottom' || h.edge === 'corner') {
                        var newH = Math.max(400, startH + dy);
                        $layer.css('height', newH + 'px');
                        self._lastHeight = newH + 'px';
                    }
                });
                $(document).on('mouseup.aiResize', function () {
                    $(document).off('mousemove.aiResize mouseup.aiResize');
                });
            });
        });
    };

    AiExpressionHelper.prototype._clearConversation = function () {
        this.conversationHistory = [];
        this.lastReasoningContent = null;
        this.lastExpression = null;
        this._chatHtml = '';
        this._initialized = false;

        var chatArea = document.getElementById('ai-chat-area');
        if (chatArea) {
            chatArea.innerHTML = '';
        }

        // 重新显示欢迎语
        this._initialized = true;
        this._appendMessage('assistant',
            '对话已清空。有什么新的需求？',
            null, new Date());
    };

    AiExpressionHelper.prototype._handleSend = function () {
        var self = this;
        var input = $('#ai-input').val().trim();
        if (!input || this.loading) return;

        var userTime = new Date();
        this.lastStartTime = userTime;
        this._appendMessage('user', input, null, userTime);
        $('#ai-input').val('');

        var currentExpression = '';
        if (this.editor) {
            currentExpression = this.editor.getValue().trim();
        }

        var requestData = {
            executorId: this.executorId,
            expressionType: this._getExpressionType(),
            currentExpression: currentExpression,
            conversationHistory: this.conversationHistory,
            newUserMessage: input
        };

        this._setLoading(true);

        var msgId = 'ai-msg-' + Date.now();
        this._appendMessage('assistant', '', msgId);

        engineUtils.requestPost(final_const.api_path.ai_generate_expression, requestData, function (response) {
            if (response.code === 200 && response.data && !response.data.error) {
                var rawResponse = response.data.rawResponse || response.data.expression || '';
                var expression = response.data.expression || '';
                var reasoningContent = response.data.reasoningContent;
                if (reasoningContent) {
                    self.lastReasoningContent = reasoningContent;
                }
                self._onGenerateDone(rawResponse, expression, input, msgId, reasoningContent);
            } else {
                var errMsg = (response.data && response.data.errorMessage) || response.message || '未知错误';
                self._updateStreamMessage(msgId, '请求失败：' + errMsg, null, null, null, null);
                self._setLoading(false);
            }
        });
    };

    AiExpressionHelper.prototype._onGenerateDone = function (rawResponse, expression, userInput, msgId, reasoningContent) {
        var self = this;
        var endTime = new Date();
        var elapsedMs = self.lastStartTime ? (endTime - self.lastStartTime) : 0;
        var elapsedStr = self._formatDuration(elapsedMs);

        self._setLoading(false);

        // 记录最近一次提取到的表达式
        if (expression) {
            self.lastExpression = expression;
        }

        self._updateStreamMessage(msgId, rawResponse, reasoningContent, endTime, elapsedStr, expression);
        self.conversationHistory.push({ role: 'user', content: userInput });
        self.conversationHistory.push({ role: 'assistant', content: rawResponse });
    };

    AiExpressionHelper.prototype._updateStreamMessage = function (msgId, content, reasoningContent, endTime, elapsedStr, expression) {
        var self = this;
        var el = document.getElementById(msgId);
        if (!el) return;

        var bottomHtml = '';

        // 时间信息
        if (endTime) {
            bottomHtml += '<div style="margin-top:6px;font-size:11px;color:#999;">' +
                '<span>' + self._formatTime(endTime) + '</span>' +
                '<span style="margin-left:8px;">耗时 ' + elapsedStr + '</span>' +
                '</div>';
        }

        // 应用表达式按钮（仅当后端提取到表达式时显示）
        if (expression) {
            var applyBtnId = 'apply-btn-' + msgId;
            bottomHtml += '<div style="margin-top:6px;">' +
                '<button class="layui-btn layui-btn-xs layui-btn-normal" id="' + applyBtnId + '">' +
                '<i class="layui-icon layui-icon-ok"></i> 应用到编辑器</button></div>';

            // 存储表达式到按钮上
            setTimeout(function () {
                var btn = document.getElementById(applyBtnId);
                if (btn) {
                    btn._expression = expression;
                    btn.addEventListener('click', function () {
                        if (self.editor && this._expression) {
                            self.editor.setValue(this._expression, -1);
                            layer.msg('已应用到编辑器');
                        }
                    });
                }
            }, 0);
        }

        // 查看思考过程按钮
        if (reasoningContent) {
            var btnId = 'reasoning-btn-' + msgId;
            bottomHtml += '<div style="margin-top:4px;">' +
                '<button class="layui-btn layui-btn-xs layui-btn-primary" id="' + btnId + '" style="padding:0 6px;font-size:11px;">' +
                '<i class="layui-icon layui-icon-dialogue"></i> 查看思考过程</button></div>';

            setTimeout(function () {
                document.getElementById(btnId).addEventListener('click', function () {
                    self._showReasoningDialog(reasoningContent, elapsedStr);
                });
            }, 0);
        }

        el.innerHTML = renderMd(content) + bottomHtml;

        var chatArea = document.getElementById('ai-chat-area');
        if (chatArea) {
            chatArea.scrollTop = chatArea.scrollHeight;
            self._chatHtml = chatArea.innerHTML;
        }
    };

    AiExpressionHelper.prototype._showReasoningDialog = function (reasoningContent, elapsedStr) {
        var formattedContent = renderMd(reasoningContent);
        var timeInfo = elapsedStr ? '<div style="margin-bottom:12px;padding:8px 12px;background:#f5f5f5;border-radius:4px;font-size:12px;color:#666;">' +
            '<i class="layui-icon layui-icon-time" style="margin-right:6px;"></i>AI 思考耗时：' + elapsedStr +
            '</div>' : '';

        layer.open({
            type: 1,
            title: 'AI 思考过程',
            area: ['600px', '500px'],
            shadeClose: true,
            content: '<div class="markdown-body" style="padding:15px;font-size:13px;line-height:1.8;color:#333;overflow-y:auto;max-height:450px;">' +
                timeInfo + formattedContent + '</div>',
            btn: ['关闭'],
            yes: function (index) {
                layer.close(index);
            }
        });
    };

    AiExpressionHelper.prototype._getExpressionType = function () {
        var select = document.querySelector('select[name="expressionType"]');
        return select ? select.value : 'condition';
    };

    AiExpressionHelper.prototype._appendMessage = function (role, content, msgId, time) {
        var self = this;
        var chatArea = document.getElementById('ai-chat-area');
        if (!chatArea) return;

        var label = role === 'user' ? '你' : 'AI';
        var bgColor = role === 'user' ? '#e8f4fd' : '#f0f9eb';
        var isUser = role === 'user';

        var contentHtml;
        if (msgId) {
            contentHtml = '<span class="ai-loading-dot">思考中</span>';
        } else if (isUser) {
            contentHtml = self._escapeHtml(content);
        } else {
            contentHtml = renderMd(content);
        }

        var timeHtml = '';
        if (time) {
            timeHtml = '<div style="font-size:11px;color:#999;margin-top:4px;">' + self._formatTime(time) + '</div>';
        }

        var avatarIcon = isUser ? 'layui-icon-username' : 'layui-icon-dialogue';
        var msgHtml = '<div style="margin-bottom:10px;display:flex;gap:8px;' + (isUser ? 'flex-direction:row-reverse;' : '') + '">' +
            '<div style="width:28px;height:28px;border-radius:50%;background:' + (isUser ? '#1e9fff' : '#16baaa') +
            ';display:flex;align-items:center;justify-content:center;flex-shrink:0;">' +
            '<i class="layui-icon ' + avatarIcon + '" style="color:#fff;font-size:14px;"></i></div>' +
            '<div style="max-width:85%;padding:8px 12px;border-radius:8px;background:' + bgColor + ';">' +
            '<div id="' + (msgId || '') + '" class="markdown-body" style="word-break:break-all;">' + contentHtml + '</div>' +
            timeHtml +
            '</div></div>';

        chatArea.innerHTML += msgHtml;
        chatArea.scrollTop = chatArea.scrollHeight;
        self._chatHtml = chatArea.innerHTML;
    };

    AiExpressionHelper.prototype._escapeHtml = function (text) {
        var div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    };

    AiExpressionHelper.prototype._formatTime = function (date) {
        var h = date.getHours().toString().padStart(2, '0');
        var m = date.getMinutes().toString().padStart(2, '0');
        var s = date.getSeconds().toString().padStart(2, '0');
        return h + ':' + m + ':' + s;
    };

    AiExpressionHelper.prototype._formatDuration = function (ms) {
        var totalSeconds = Math.floor(ms / 1000);
        var minutes = Math.floor(totalSeconds / 60);
        var seconds = totalSeconds % 60;
        if (minutes > 0) {
            return minutes + '分' + seconds + '秒';
        } else {
            return seconds + '秒';
        }
    };

    AiExpressionHelper.prototype._setLoading = function (loading) {
        this.loading = loading;
        var btn = document.getElementById('ai-send-btn');
        if (btn) {
            btn.disabled = loading;
            btn.innerHTML = loading
                ? '<i class="layui-icon layui-icon-loading layui-anim layui-anim-rotate layui-anim-loop"></i> 思考中...'
                : '<i class="layui-icon layui-icon-release"></i> 发送';
        }
    };

    return AiExpressionHelper;
})();
