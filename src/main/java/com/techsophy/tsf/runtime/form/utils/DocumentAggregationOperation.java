package com.techsophy.tsf.runtime.form.utils;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

public class DocumentAggregationOperation implements AggregationOperation
{
    private final Document aggregationOperationDocument;

    public DocumentAggregationOperation(String jsonOperation)
    {
        this.aggregationOperationDocument = Document.parse(jsonOperation);
    }

    public DocumentAggregationOperation(Document aggregationOperationDocument)
    {
        this.aggregationOperationDocument = aggregationOperationDocument;
    }

    @Override
    public Document toDocument(AggregationOperationContext context)
    {
        return context.getMappedObject(aggregationOperationDocument);
    }
}
