/**
 * Ace Editor 表达式编辑器封装类
 *
 * 功能：
 * - 提供基于 Ace Editor 的表达式编辑器
 * - 支持语法高亮（JavaScript 模式，兼容 Aviator 表达式）
 * - 支持自动补全（通过后端 API 获取函数和变量）
 * - 自动同步到隐藏的 textarea（用于表单提交）
 *
 * 使用示例：
 * <pre>
 * var editor = new AceExpressionEditor({
 *     containerId: 'expressionEditor',
 *     textareaId: 'expression_input_search',
 *     executorId: 123,
 *     expressionId: 456,
 *     apiPath: '/expression-engine/doc/getList'
 * });
 * editor.init();
 * </pre>
 *
 * @author Claude
 * @date 2025-01-26
 */
var AceExpressionEditor = (function () {

    /**
     * 构造函数
     *
     * @param config 配置对象
     * @param config.containerId 编辑器容器 DOM ID
     * @param config.textareaId 同步内容的 textarea ID
     * @param config.executorId 执行器 ID（用于 API 请求）
     * @param config.expressionId 表达式 ID（可选，用于 API 请求）
     * @param config.apiPath 自动补全 API 路径
     * @param config.height 编辑器高度（默认：200px）
     * @param config.theme 编辑器主题（默认：chrome）
     * @param config.mode 编辑器语言模式（默认：javascript）
     * @param config.fontSize 字体大小（默认：14px）
     * @param config.enableLiveAutocompletion 是否启用实时补全（默认：true）
     * @param config.enableBasicAutocompletion 是否启用基础补全（默认：true）
     */
    function AceExpressionEditor(config) {
        // 配置参数
        this.config = {
            containerId: config.containerId || 'expressionEditor',
            textareaId: config.textareaId || 'expression_input_search',
            executorId: config.executorId,
            expressionId: config.expressionId || null,
            apiPath: config.apiPath || '/expression-engine/doc/getList',
            height: config.height || '200px',
            theme: config.theme || 'ace/theme/chrome',
            mode: config.mode || 'ace/mode/javascript',
            fontSize: config.fontSize || '14px',
            enableLiveAutocompletion: config.enableLiveAutocompletion !== false,
            enableBasicAutocompletion: config.enableBasicAutocompletion !== false
        };

        // 内部状态
        this.editor = null;
        this.langTools = null;
        this.completerCache = {};
    }

    /**
     * 初始化编辑器
     */
    AceExpressionEditor.prototype.init = function () {
        var self = this;

        // 检查容器是否存在
        var container = document.getElementById(this.config.containerId);
        if (!container) {
            console.error('AceExpressionEditor: 容器 ' + this.config.containerId + ' 不存在');
            return;
        }

        // 设置容器高度
        container.style.height = this.config.height;

        // 创建 Ace Editor 实例
        this.editor = ace.edit(this.config.containerId);

        // 配置编辑器
        this.editor.setTheme(this.config.theme);
        this.editor.session.setMode(this.config.mode);
        this.editor.setOptions({
            fontSize: this.config.fontSize,
            showPrintMargin: false,
            enableBasicAutocompletion: this.config.enableBasicAutocompletion,
            enableLiveAutocompletion: this.config.enableLiveAutocompletion,
            // 设置实时补全延迟，确保每次输入都能触发后端请求
            liveAutocompletionDelay: 300,
            liveAutocompletionThreshold: 2,  // 输入1个字符就触发
            wrap: true,
            autoScrollEditorIntoView: true
        });

        // 扩展 JavaScript 模式以支持 Aviator lambda 语法
        this._extendModeForAviator();

        // 同步内容到 textarea
        this._bindTextareaSync();

        // 设置初始值
        this._setInitialValue();

        // 注册自定义补全器
        this._registerCustomCompleter();

        // 修复 wheel 事件警告 - 使事件监听器变为 passive
        this._fixWheelEventPassive();

        console.log('AceExpressionEditor 初始化完成');
    };

    /**
     * 绑定 textarea 同步
     * @private
     */
    AceExpressionEditor.prototype._bindTextareaSync = function () {
        var self = this;
        var $ = layui.$;

        // 编辑器内容变化时同步到 textarea
        this.editor.session.on('change', function () {
            $('#' + self.config.textareaId).val(self.editor.getValue());
        });

        // 监听 textarea 的变化（外部修改时同步到编辑器）
        $('#' + self.config.textareaId).on('input', function () {
            var currentVal = self.editor.getValue();
            var newVal = $(this).val();
            if (currentVal !== newVal) {
                self.editor.setValue(newVal, -1);
                self.editor.clearSelection();
            }
        });
    };

    /**
     * 设置初始值
     * @private
     */
    AceExpressionEditor.prototype._setInitialValue = function () {
        var $ = layui.$;
        var initialValue = $('#' + this.config.textareaId).val();

        if (initialValue) {
            this.editor.setValue(initialValue, -1);
        } else {
            this.editor.setValue('true', -1);  // 默认值
        }
        this.editor.clearSelection();
    };

    /**
     * 注册自定义补全器
     * @private
     */
    AceExpressionEditor.prototype._registerCustomCompleter = function () {
        var self = this;

        // 获取语言工具模块
        this.langTools = ace.require('ace/ext/language_tools');

        // 禁用关键字补全器
        this._disableKeywordCompleter();

        // 创建并注册自定义补全器
        var customCompleter = this._createCustomCompleter();
        this.langTools.addCompleter(customCompleter);

        // 将自定义补全器移到第一位
        this._prioritizeCompleter(customCompleter);

        console.log('AceExpressionEditor: 自定义补全器已注册');
    };

    /**
     * 禁用关键字补全器
     * @private
     */
    AceExpressionEditor.prototype._disableKeywordCompleter = function () {
        if (this.langTools.keyWordCompleter) {
            var self = this;
            this.langTools.keyWordCompleter.getCompletions = function (editor, session, pos, prefix, callback) {
                callback(null, []);
            };
            console.log('AceExpressionEditor: 已禁用关键字补全器');
        }
    };

    /**
     * 创建自定义补全器
     * @private
     * @returns {Object} Ace Editor 补全器对象
     */
    AceExpressionEditor.prototype._createCustomCompleter = function () {
        var self = this;

        return {
            identifierRegexs: [/[a-zA-Z_0-9$\.\u4e00-\u9fa5]/],
            getCompletions: function (editor, session, pos, prefix, callback) {
                // 获取当前输入的关键字
                var keyword = self._extractKeyword(session, pos);

                // 关键字太短，不进行补全
                if (keyword.length < 1) {
                    callback(null, []);
                    return;
                }

                // 禁用缓存，每次都请求后端获取最新的前缀匹配结果
                // if (self.completerCache[keyword]) {
                //     callback(null, self.completerCache[keyword]);
                //     return;
                // }

                // 请求后端 API
                self._fetchCompletions(keyword, callback);
            }
        };
    };

    /**
     * 从编辑器会话中提取关键字
     * @private
     * @param session Ace Editor session 对象
     * @param pos 光标位置 {row, column}
     * @returns {string} 提取的关键字
     */
    AceExpressionEditor.prototype._extractKeyword = function (session, pos) {
        var line = session.getLine(pos.row);
        var cursor = pos.column;
        var start = cursor - 1;

        // 从光标位置向前查找，获取关键字
        while (start >= 0 && /[\w.]/.test(line[start])) {
            start--;
        }

        return line.substring(start + 1, cursor).trim();
    };

    /**
     * 从后端 API 获取补全数据
     * @private
     * @param keyword 关键字
     * @param callback 回调函数
     */
    AceExpressionEditor.prototype._fetchCompletions = function (keyword, callback) {
        var self = this;
        var $ = layui.$;

        var url = this.config.apiPath +
            '?executorId=' + this.config.executorId +
            '&expressionId=' + (this.config.expressionId || '') +
            '&limit=20&name=' + encodeURIComponent(keyword);

        engineUtils.requestGetSilent(url, function (response) {
            if (response && response.code === 200 && response.data) {
                var completions = self._buildCompletions(response.data, keyword);
                self.completerCache[keyword] = completions;
                callback(null, completions);
            } else {
                callback(null, []);
            }
        });
    };

    /**
     * 构建补全项列表
     * @private
     * @param data 后端返回的数据
     * @param keyword 搜索关键字
     * @returns {Array} 补全项数组
     */
    AceExpressionEditor.prototype._buildCompletions = function (data, keyword) {
        var completions = [];

        for (var i = 0; i < data.length; i++) {
            var item = data[i];
            var meta = item.type === 'fn' ? '函数' : '变量';
            var caption = item.name;
            var value = item.name;

            // 函数类型自动添加括号
            if (item.type === 'fn') {
                value = item.name + '()';
            }

            // 构建描述文本
            var docText = this._buildDocText(item);

            completions.push({
                caption: caption,
                value: value,
                meta: meta,
                score: 1000,
                docText: docText,
                docHTML: this._buildDocHTML(item, keyword)
            });
        }

        return completions;
    };

    /**
     * 构建纯文本描述
     * @private
     * @param item 补全项数据
     * @returns {string} 纯文本描述
     */
    AceExpressionEditor.prototype._buildDocText = function (item) {
        var docText = '';

        if (item.describe) {
            docText += '描述: ' + item.describe + '\n';
        }
        if (item.type === 'fn' && item.params) {
            var paramsText = Array.isArray(item.params) ? item.params.join(', ') : item.params;
            if (paramsText) {
                docText += '参数: ' + paramsText + '\n';
            }
        }
        if (item.example) {
            docText += '示例: ' + item.example + '\n';
        }
        if (item.groupName) {
            docText += '分组: ' + item.groupName;
        }

        return docText;
    };

    /**
     * 构建 HTML 文档
     * @private
     * @param item 补全项数据
     * @param keyword 搜索关键字
     * @returns {string} HTML 文档
     */
    AceExpressionEditor.prototype._buildDocHTML = function (item, keyword) {
        var html = '<div style="padding: 8px; max-width: 450px;">';

        // 标题行 - 名称和类型
        html += '<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; border-bottom: 1px solid #e0e0e0; padding-bottom: 5px;">';
        html += '<div style="font-weight: bold; color: #1e9fff; font-size: 15px;">' + this._escapeHtml(item.name) + '</div>';
        html += '<div style="color: #666; font-size: 12px; background: #f0f0f0; padding: 2px 8px; border-radius: 3px;">' +
            (item.type === 'fn' ? '函数' : '变量') + '</div>';
        html += '</div>';

        // 描述
        if (item.describe) {
            html += '<div style="margin-bottom: 8px; color: #333; line-height: 1.5;">';
            html += '<span style="color: #999; font-size: 12px;">描述:</span> ';
            html += this._highlightKeyword(this._escapeHtml(item.describe), keyword);
            html += '</div>';
        }

        // 参数
        if (item.type === 'fn' && item.params) {
            var paramsText = this._formatParams(item.params);
            if (paramsText) {
                html += '<div style="margin-bottom: 8px;">';
                html += '<span style="color: #999; font-size: 12px;">参数:</span> ';
                html += '<code style="background: #f5f5f5; padding: 3px 6px; border-radius: 3px; color: #d63384;">';
                html += this._escapeHtml(paramsText);
                html += '</code></div>';
            }
        }

        // 示例
        if (item.example) {
            html += '<div style="margin-bottom: 8px;">';
            html += '<span style="color: #999; font-size: 12px;">示例:</span><br/>';
            html += '<code style="background: rgb(45 63 38 / 8%); color: #2835e3; padding: 6px 10px; border-radius: 4px; display: inline-block; margin-top: 4px; font-size: 13px;">';
            html += this._escapeHtml(item.example);
            html += '</code></div>';
        }

        // 分组信息
        if (item.groupName) {
            html += '<div style="margin-top: 8px; padding-top: 8px; border-top: 1px dashed #e0e0e0; color: #999; font-size: 12px;">';
            html += '<span style="margin-right: 15px;"><i class="layui-icon layui-icon-group" style="font-size: 12px;"></i> ' +
                this._escapeHtml(item.groupName) + '</span>';
            html += '<span>来自: ' + this._escapeHtml(item.serviceName || '系统') + '</span>';
            html += '</div>';
        }

        html += '</div>';
        return html;
    };

    /**
     * 格式化参数列表
     * @private
     * @param params 参数（数组或字符串）
     * @returns {string} 格式化后的参数字符串
     */
    AceExpressionEditor.prototype._formatParams = function (params) {
        if (Array.isArray(params)) {
            return params.length > 0 ? params.join(', ') : '';
        } else if (typeof params === 'string' && params.trim() !== '') {
            return params;
        }
        return '';
    };

    /**
     * 高亮关键字
     * @private
     * @param text 文本
     * @param keyword 关键字
     * @returns {string} 高亮后的 HTML
     */
    AceExpressionEditor.prototype._highlightKeyword = function (text, keyword) {
        if (!keyword || text.toLowerCase().indexOf(keyword.toLowerCase()) === -1) {
            return text;
        }
        var regex = new RegExp('(' + this._escapeRegex(keyword) + ')', 'gi');
        return text.replace(regex, '<mark style="background: #ffeb3b; padding: 1px 2px;">$1</mark>');
    };

    /**
     * 转义 HTML 特殊字符
     * @private
     * @param text 文本
     * @returns {string} 转义后的文本
     */
    AceExpressionEditor.prototype._escapeHtml = function (text) {
        if (!text) return '';
        return String(text)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    /**
     * 转义正则表达式特殊字符
     * @private
     * @param text 文本
     * @returns {string} 转义后的文本
     */
    AceExpressionEditor.prototype._escapeRegex = function (text) {
        if (!text) return '';
        return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    };

    /**
     * 将补全器移到第一位（优先级最高）
     * @private
     * @param completer 补全器对象
     */
    AceExpressionEditor.prototype._prioritizeCompleter = function (completer) {
        if (this.langTools.completers && this.langTools.completers.length > 0) {
            var idx = this.langTools.completers.indexOf(completer);
            if (idx > 0) {
                this.langTools.completers.splice(idx, 1);
                this.langTools.completers.unshift(completer);
            }
        }
    };

    /**
     * 扩展 JavaScript 模式以支持 Aviator lambda 语法
     * Aviator lambda 语法: lambda -> lambda + 1, (x, y) -> x + y
     * @private
     */
    AceExpressionEditor.prototype._extendModeForAviator = function () {
        var self = this;

        // 等待 mode 完全加载后扩展
        setTimeout(function () {
            try {
                var JavaScriptHighlightRules = ace.require('ace/mode/javascript_highlight_rules').JavaScriptHighlightRules;
                var oop = ace.require('ace/lib/oop');

                // 创建自定义 Aviator 高亮规则
                var AviatorHighlightRules = function () {
                    JavaScriptHighlightRules.call(this);
                    this.$rules.start.unshift({
                        token: 'storage.type.function',
                        regex: '\\blambda\\b'
                    }, {
                        token: 'keyword.operator',
                        regex: '->'
                    });
                };
                oop.inherits(AviatorHighlightRules, JavaScriptHighlightRules);

                // 创建自定义 Mode
                var AviatorMode = function () {
                    ace.require('ace/mode/javascript').Mode.call(this);
                    this.HighlightRules = AviatorHighlightRules;
                };
                oop.inherits(AviatorMode, ace.require('ace/mode/javascript').Mode);

                self.editor.session.setMode(new AviatorMode());

                // 拦截 session 的 setAnnotations 方法，过滤 lambda 相关错误
                var originalSetAnnotations = self.editor.session.setAnnotations;
                self.editor.session.setAnnotations = function (annotations) {
                    var filtered = [];

                    for (var i = 0; i < annotations.length; i++) {
                        var anno = annotations[i];
                        var line = self.editor.session.getLine(anno.row);

                        // 如果包含 lambda 语法，过滤掉相关的语法错误
                        if (line.indexOf('->') > -1 || line.indexOf('lambda') > -1) {
                            if (anno.text && (
                                anno.text.indexOf('Unexpected token') > -1 ||
                                anno.text.indexOf('Missing') > -1 ||
                                anno.text.indexOf('Expected') > -1 ||
                                anno.text.indexOf('arrow') > -1 ||
                                anno.text.indexOf('=>') > -1
                            )) {
                                continue;
                            }
                        }
                        filtered.push(anno);
                    }

                    originalSetAnnotations.call(this, filtered);
                };

                console.log('AceExpressionEditor: 已扩展支持 Aviator lambda 语法 (->)');
            } catch (e) {
                console.warn('AceExpressionEditor: 扩展 Aviator 语法失败', e);
            }
        }, 100);
    };

    /**
     * 修复 wheel 和 touchmove 事件警告 - 使事件监听器变为 passive
     * 解决 Chrome 警告:
     * - [Violation] Added non-passive event listener to a scroll-blocking 'wheel' event
     * - [Violation] Added non-passive event listener to a scroll-blocking 'touchmove' event
     * @private
     */
    AceExpressionEditor.prototype._fixWheelEventPassive = function () {
        if (!this.editor || !this.editor.container) {
            return;
        }

        var container = this.editor.container;

        try {
            // 使用 setTimeout 确保在 Ace 完成初始化后再执行
            setTimeout(function () {
                // 方案 1: 使用 CSS touch-action 属性优化滚动
                container.style.touchAction = 'pan-y pan-x';
                container.style.overscrollBehavior = 'contain';

                // 方案 2: 添加 passive 的事件监听器来覆盖默认行为
                // 这些监听器不会阻止默认行为，因此可以是 passive 的
                container.addEventListener('wheel', function () {}, { passive: true });
                container.addEventListener('touchmove', function () {}, { passive: true });
                container.addEventListener('touchstart', function () {}, { passive: true });
                container.addEventListener('touchend', function () {}, { passive: true });
                container.addEventListener('pointerdown', function () {}, { passive: true });

                console.log('AceExpressionEditor: 滚动事件已优化为 passive 模式');
            }, 100);
        } catch (e) {
            console.warn('AceExpressionEditor: 无法优化滚动事件', e);
        }
    };

    // ========================
    // 公共 API 方法
    // ========================

    /**
     * 获取编辑器内容
     * @returns {string} 编辑器内容
     */
    AceExpressionEditor.prototype.getValue = function () {
        return this.editor ? this.editor.getValue() : '';
    };

    /**
     * 设置编辑器内容
     * @param value 内容
     * @param cursorPosition 光标位置（默认：-1，移动到末尾）
     */
    AceExpressionEditor.prototype.setValue = function (value, cursorPosition) {
        if (this.editor) {
            this.editor.setValue(value, cursorPosition || -1);
            this.editor.clearSelection();
        }
    };

    /**
     * 获取 Ace Editor 实例（用于高级操作）
     * @returns {Object} Ace Editor 实例
     */
    AceExpressionEditor.prototype.getAceEditor = function () {
        return this.editor;
    };

    /**
     * 清空补全缓存
     */
    AceExpressionEditor.prototype.clearCache = function () {
        this.completerCache = {};
    };

    /**
     * 销毁编辑器
     */
    AceExpressionEditor.prototype.destroy = function () {
        if (this.editor) {
            this.editor.destroy();
            this.editor = null;
        }
    };

    // 返回构造函数
    return AceExpressionEditor;

})();
