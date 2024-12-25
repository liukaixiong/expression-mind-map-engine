package com.liukx.expression.engine.client.process;


import com.liukx.expression.engine.client.api.DocumentApiExecutor;
import com.liukx.expression.engine.core.api.model.ExpressionService;

public abstract class AbstractExpressionService implements ExpressionService {
    private DocumentApiExecutor documentApiExecutor;

    private String nameSpace;

    public AbstractExpressionService(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public DocumentApiExecutor getDocumentApiExecutor() {
        return documentApiExecutor;
    }

    public void setDocumentRegister(DocumentApiExecutor documentRegister) {
        this.documentApiExecutor = documentRegister;
    }

}
