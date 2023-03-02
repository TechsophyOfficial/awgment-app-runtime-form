package com.techsophy.tsf.runtime.form.utils;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.COLON;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.COMMA;
@Component
public class RelationUtils
{
    public List<String> getSplitOffRelations(String relations)
    {
        if(relations!=null)
        {
            List<String> formIdList=new ArrayList<>();
            Arrays.stream(relations.split(COMMA)).forEach(x-> formIdList.add(x.split(COLON)[0]));
            Arrays.stream(relations.split(COMMA)).forEach(x->formIdList.add(x.split(COLON)[1]));
            return formIdList;
        }
       return Collections.emptyList();
    }
}
