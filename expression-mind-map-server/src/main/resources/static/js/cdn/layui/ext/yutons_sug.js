/**
 * @Title：yutons_sug搜索框提示插件||输入框提示插件
 * @Version：1.0
 * @Auth：yutons
 * @Date: 2018/10/4
 * @Time: 14:07
 */
layui.define(['jquery', 'table'], function (exports) {
    "use strict";
    const $ = layui.jquery,
        table = layui.table;

    let yutons_sug = function () {
        this.v = '1.0.1';
    };

    let globalOpt = {};

    let currentIndex = -1;
    let isEnterSearch = false;
    /**
     * yutons_sug搜索框提示插件||输入框提示插件初始化
     */
    yutons_sug.prototype.render = function (customerOpt) {
        globalOpt = $.extend(globalOpt, customerOpt);
        globalOpt.urlBak = customerOpt.url;
        globalOpt.id = customerOpt.id;
        //设置默认初始化参数
        globalOpt.type = customerOpt.type || 'sug'; //默认sug，传入sug||sugTable
        globalOpt.elem = "#yutons_sug_" + customerOpt.id;
        globalOpt.height = customerOpt.height || '229';
        globalOpt.cellMinWidth = customerOpt.cellMinWidth || '10'; //最小列宽
        globalOpt.page = customerOpt.page || false;
        globalOpt.limits = customerOpt.limits || [10];
        globalOpt.loading = customerOpt.loading || true;
        globalOpt.limit = customerOpt.limit || 10;
        globalOpt.size = customerOpt.size || 'sm'; //小尺寸的表格
        globalOpt.showText = customerOpt.showText || function (res) {
            return res;
        };

        //初始化输入框提示容器
        getJqOptId(globalOpt).after("<div id='sugItem' style='background-color: #e6e6e6;display: none;z-index:1;position: absolute;width:100%;'></div>");

        //输入框提示容器移出事件：鼠标移出隐藏输入提示框
        getJqOptId(globalOpt).parent().mouseleave(function () {
            getJqOptId(globalOpt).next().hide().html("");
        });


        if (globalOpt.type === "sugTable") {
            //如果type为sugTable，则初始化下拉表格
            /* 输入框鼠标松开事件 */
            // $(`#${opt.id}`).mouseup(function (e) {
            //     opt.obj = this;
            //     getSugTable(opt);
            // })
            // /* 输入框键盘抬起事件 */
            // $(`#${opt.id}`).keyup(function (e) {
            //     opt.obj = this;
            //     getSugTable(opt);
            // })

            getJqOptId(globalOpt).keydown(function (event) {
                if (event.ctrlKey && event.altKey && event.key === ' ') {
                    globalOpt.obj = this;
                    isEnterSearch = true;
                    getSugTable(event, globalOpt);
                }
            })
        } else if (globalOpt.type === "sug") {
            //如果type为sug，则初始化下拉框
            getJqOptId(globalOpt).next().css("border", "solid #e6e6e6 0.5px");
            /* 输入框鼠标松开事件 */
            // $(`#${opt.id}`).mouseup(function (e) {
            //     opt.obj = this;
            //     getSug(opt);
            // })
            /* 输入框键盘抬起事件 */
            // $(`#${opt.id}`).keyup(function (e) {
            //     opt.obj = this;
            //     getSug(opt);
            // })
            getJqOptId(globalOpt).keydown(function (event) {
                if (event.ctrlKey && event.altKey && event.key === ' ') {
                    globalOpt.obj = this;
                    getSug(event, globalOpt);
                }
            })
        }
    }

    function getJqOptId(opt) {
        return $(`#${opt.id}`);
    }

    //搜索框提示插件||输入框提示插件--sugTable-下拉表格
    function getSugTable(event, opt) {
        sessionStorage.setItem("inputId", opt.id)
        let endIndex = event.target.selectionStart;
        //如果输入信息为"",则隐藏输入提示框,不再执行下边代码
        let kw = $.trim($(opt.obj).val());
        let optCurrentId = opt.obj.getAttribute("id");
        let searchKeyword = getLastPartAfterLastSpace(kw, endIndex);
        if (searchKeyword === "") {
            $(`#${optCurrentId}`).next().hide().html("");
            return false;
        }
        //下拉表格初始化table容器
        let html = '<table class="item" id="yutons_sug_' + optCurrentId + '" lay-filter="yutons_sug_' + optCurrentId +
            '"></table>';

        $(`#${optCurrentId}`).next().show().html(html);

        currentIndex = -1;
        //下拉表格初始化
        opt.url = opt.urlBak + searchKeyword;
        table.render(opt);

        //设置下拉表格样式
        // $(globalOpt.elem).next().css("margin-top", "0").css("background-color", "#ffffff");
        //监听下拉表格行单击事件（单击||双击事件为：row||rowDouble）设置单击或双击选中对应的行
        table.on('rowDouble(' + "yutons_sug_" + opt.id + ')', function (obj) {
            //获取选中行所传入字段的值
            replaceSubCheckValue(endIndex, obj.data);
        });
    }

    //搜索框提示插件||输入框提示插件--sug-下拉框
    function getSug(event, opt) {
        sessionStorage.setItem("inputId", opt.id);
        let endIndex = event.target.selectionStart;
        let kw = $.trim($(opt.obj).val());
        let searchKeyWord = getLastPartAfterLastSpace(kw, endIndex);
        if (kw === "") {
            $("#" + opt.id).next().hide().html("");
            return false;
        }
        //sug下拉框异步加载数据并渲染下拉框
        $.ajax({
            type: "get",
            url: opt.urlBak + searchKeyWord,
            success: function (data) {
                let html = "";
                layui.each(data, (index, item) => {
                    //if (item[sessionStorage.getItem("inputId")].indexOf(decodeURI(kw)) >= 0) {
                    html +=
                        "<div class='item' style='padding: 3px 10px;cursor: pointer;' onmouseenter='getFocus(this)' onClick='getCon(this);'>" +
                        opt.tableParseData(item) + "</div>";
                    //}
                });
                if (html !== "") {
                    $("#" + sessionStorage.getItem("inputId")).next().show().html(html);
                } else {
                    $("#" + sessionStorage.getItem("inputId")).next().hide().html("");
                }
            }
        });
    }

    function getLastPartAfterLastSpace(str, endIndex) {

        // 找到最后一个空格的位置
        // const lastSpaceIndex = str.lastIndexOf(' ');
        let lastSpaceIndex = -1;

        for (let i = endIndex - 1; i >= 0; i--) {
            if (str[i] === ' ') {
                lastSpaceIndex = i;
                break;
            }
        }

        // 如果没有找到空格，返回整个字符串
        if (lastSpaceIndex === -1 && endIndex === 0) {
            return str;
        }

        // 提取最后一个空格之后的字符串
        let keywordText = str.substring(lastSpaceIndex + 1, endIndex);


        // 语法关键字清理
        let symbolKey = [")", "(", "&", "|", ",", "'", " "];

        symbolKey.forEach(item => {
            keywordText = keywordText.replaceAll(item, "");
        })

        return keywordText;
    }


    function reloadTableIndexCheck(index) {
        let inputId = sessionStorage.getItem("inputId");
        let data = table.getData(inputId);
        let indexLength = data.length;

        if (index >= indexLength) {
            currentIndex = 0;
            index = 0;
        } else if (index <= -1) {
            currentIndex = indexLength - 1;
            index = indexLength - 1;
        }

        if (index > -1) {
            table.setRowChecked(inputId, {
                index: 'all', // 选中行的下标。 0 表示第一行
                checked: false
            });
        }
        table.setRowChecked(inputId, {
            index: index, // 选中行的下标。 0 表示第一行
            checked: true
        });
        table.renderData(inputId);
    }


    function replaceSubCheckValue(cursorIndex, checkObj) {
        let htmlObj = $("#" + sessionStorage.getItem("inputId"));
        let sourceValue = htmlObj.val();

        let inputKeywordLength = getLastPartAfterLastSpace(sourceValue, cursorIndex).length;

        let leftEnd = cursorIndex - inputKeywordLength;

        let inputValue = globalOpt.showText(checkObj);

        const newValue = sourceValue.slice(0, leftEnd) + inputValue + sourceValue.slice(cursorIndex);

        htmlObj.val(newValue);
        htmlObj.next().hide().html("");
    }

    //搜索框提示插件||输入框提示插件--sug-下拉框上下键移动事件和回车事件
    $(document).keydown(function (e) {
        e = e || window.event;
        let keycode = e.which ? e.which : e.keyCode;
        // 待优化
        if (true) {
            let inputId = sessionStorage.getItem("inputId");
            if (e.target.id !== inputId) {
                return;
            }
            if (keycode === 38) {
                reloadTableIndexCheck(--currentIndex);
                if (isEnterSearch) {
                    e.preventDefault();
                }
                // e.preventDefault();
            } else if (keycode === 40) {
                reloadTableIndexCheck(++currentIndex);
                if (isEnterSearch) {
                    e.preventDefault();
                }
                // e.preventDefault();
            } else if (keycode === 13) {
                if (isEnterSearch) {
                    e.preventDefault();
                }
                isEnterSearch = false;
                let checkObj = table.checkStatus(inputId);
                if (checkObj.data.length === 0) {
                    return;
                }
                table.setRowChecked(inputId, {
                    index: 'all', // 选中行的下标。 0 表示第一行
                    checked: false
                });
                let cursorIndex = e.target.selectionStart;
                replaceSubCheckValue(cursorIndex, checkObj.data[0]);
                currentIndex = -1;
                // e.preventDefault();
            }
        } else {
            if (keycode === 38) {
                //上键事件
                if ($.trim($("#" + inputId).next().html()) == "") {
                    return;
                }
                movePrev(sessionStorage.getItem("inputId"));
            } else if (keycode === 40) {
                //下键事件
                if ($.trim($("#" + inputId).next().html()) == "") {
                    return;
                }
                // $("#" + sessionStorage.getItem("inputId")).blur();
                if ($(".item").hasClass("addbg")) {
                    moveNext();
                } else {
                    $(".item").removeClass('addbg').css("background", "").eq(0).addClass('addbg').css("background", "#e6e6e6");
                }
            } else if (keycode === 13) {
                //回车事件
                dojob();
            }
        }
    });
    //上键事件
    let movePrev = function (id) {
        $("#" + id).blur();
        let index = $(".addbg").prevAll().length;
        if (index === 0) {
            $(".item").removeClass('addbg').css("background", "").eq($(".item").length - 1).addClass('addbg').css(
                "background", "#e6e6e6");
        } else {
            $(".item").removeClass('addbg').css("background", "").eq(index - 1).addClass('addbg').css("background", "#e6e6e6");
        }
    }
    //下键事件
    let moveNext = function () {
        let index = $(".addbg").prevAll().length;
        if (index === $(".item").length) {
            $(".item").removeClass('addbg').css("background", "").eq(0).addClass('addbg').css("background", "#e6e6e6");
        } else {
            $(".item").removeClass('addbg').css("background", "").eq(index + 1).addClass('addbg').css("background", "#e6e6e6");
        }
    }
    //回车事件
    let dojob = function () {
        let value = $(".addbg").text();
        $("#" + sessionStorage.getItem("inputId")).blur();
        $("#" + sessionStorage.getItem("inputId")).val(value);
        $("#" + sessionStorage.getItem("inputId")).next().hide().html("");
    }

    //上下键选择和鼠标选择事件改变颜色
    window.getFocus = function (obj) {
        $(".item").css("background", "");
        $(obj).css("background", "#e6e6e6");
    }

    //点击选中事件，获取选中内容并回显到输入框
    window.getCon = function (obj) {
        let value = $(obj).text();
        $("#" + $(".item").parent().prev().attr("id")).val(value);
        $("#" + $(".item").parent().prev().attr("id")).next().hide().html("");
    }

    //自动完成渲染
    yutons_sug = new yutons_sug();
    //暴露方法
    exports("yutons_sug", yutons_sug);
});
