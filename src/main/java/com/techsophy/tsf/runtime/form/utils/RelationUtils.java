package com.techsophy.tsf.runtime.form.utils;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.COLON;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.COMMA;
@Component
public class RelationUtils
{
    public List<String> getListOfFormIdsUsingRelations(String relations)
    {
        List<String> formIdList=new ArrayList<>();
        Arrays.stream(relations.split(COMMA)).forEach(x-> formIdList.add(x.split(COLON)[0]));
        return formIdList;
    }
}
