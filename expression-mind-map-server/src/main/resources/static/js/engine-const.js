/**
 *  定义 api 路径
 *
 * @author liukx
 * @date  -
 */
let final_const = {

    api_path: {

        executor_add: '/smp-engine/executor/info/addOne',

        executor_edit: '/smp-engine/executor/info/editOne',

        executor_del: '/smp-engine/executor/info/batchDelete',

        executor_info: '/smp-engine/executor/info/findExecutorInfo',

        executor_list: '/smp-engine/executor/info/findExecutorList',

        executor_doc_type_list: '/smp-engine/doc/getTypeList',

        executor_doc_search_list: '/smp-engine/doc/getList',

        /**
         * 下载json文件
         */
        download_json: '/smp-engine/components/exportData',

        /**
         * 上传json文件
         */
        upload_json: '/smp-engine/components/importData',

        trace_list: '/smp-engine/executor/trace/list',
        /**
         * 获取表达式列表
         */
        expression_list: '/smp-engine/executor/expression/findExpressionList',
        /**
         * 获取用户详情
         */
        expression_findExpressionInfo: '/smp-engine/executor/expression/findExpressionInfo',
        /**
         * 添加表达式
         */
        expression_add: '/smp-engine/executor/expression/addOne',
        /**
         * 修改表达式
         */
        expression_edit: '/smp-engine/executor/expression/editOne',
        expression_del: '/smp-engine/executor/expression/batchDelete',
        expression_edit_parent: '/smp-engine/executor/expression/editParentId',
        expression_copy_node: '/smp-engine/executor/expression/copyNode',
    },

    template_path: {
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

