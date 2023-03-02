package com.techsophy.tsf.runtime.form.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;

@ExtendWith({MockitoExtension.class})
class RelationUtilsTest
{
    @InjectMocks
    RelationUtils mockRelationUtils;
    @Test
    void getSplitOffRelationsTest()
    {
        String relations="994102731543871488:orderId,994122561634369536:parcelId";
        List<String> relationsList=new ArrayList();
        relationsList.add("994102731543871488");
        relationsList.add("994122561634369536");
        relationsList.add("orderId");
        relationsList.add("parcelId");
        Assertions.assertEquals(relationsList,mockRelationUtils.getSplitOffRelations(relations));
    }

    @Test
    void getSplitOffRelationsNullTest()
    {
        Assertions.assertTrue(mockRelationUtils.getSplitOffRelations(null).isEmpty());
    }
}
