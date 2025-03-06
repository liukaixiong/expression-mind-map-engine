/**
 *  定义 api 路径
 *
 * @author liukx
 * @date  -
 */
let final_const = {

    api_path: {
        login: '/login',

        executor_add: '/expression-engine/executor/info/addOne',

        executor_edit: '/expression-engine/executor/info/editOne',

        executor_del: '/expression-engine/executor/info/batchDelete',

        executor_info: '/expression-engine/executor/info/findExecutorInfo',

        executor_list: '/expression-engine/executor/info/findExecutorList',

        executor_doc_type_list: '/expression-engine/doc/getTypeList',

        executor_doc_search_list: '/expression-engine/doc/getList',

        /**
         * 表达式翻译
         */
        executor_doc_translate_list: '/expression-engine/doc/translateExpression',

        /**
         * 下载json文件
         */
        download_json: '/expression-engine/components/exportData',

        /**
         * 上传json文件
         */
        upload_json: '/expression-engine/components/importData',

        trace_list: '/expression-engine/executor/trace/list',

        /**
         * 获取单个追踪信息
         */
        trace_object: '/expression-engine/executor/trace/info',
        /**
         * 获取表达式的样本参数
         */
        trace_sample_object: '/expression-engine/executor/trace/getExpressionSampleBody',
        /**
         * 获取表达式列表
         */
        expression_list: '/expression-engine/executor/expression/findExpressionList',
        /**
         * 获取用户详情
         */
        expression_findExpressionInfo: '/expression-engine/executor/expression/findExpressionInfo',
        /**
         * 添加表达式
         */
        expression_add: '/expression-engine/executor/expression/addOne',
        /**
         * 修改表达式
         */
        expression_edit: '/expression-engine/executor/expression/editOne',
        expression_del: '/expression-engine/executor/expression/batchDelete',
        expression_edit_parent: '/expression-engine/executor/expression/editParentId',
        expression_copy_node: '/expression-engine/executor/expression/copyNode',
    },

    template_path: {
        index: '/template/executor-list.html',
        executor_form: '/template/executor-form.html',
        expression_rule_config: '/template/expression-rule-config.html',
        expression_form: '/template/expressionForm.html',

    },
    final_constants: {
        /**
         * 定义表达式类型集合
         */
        expression_type_list: ["action", "condition", "trigger", "callback"]
    }


}

