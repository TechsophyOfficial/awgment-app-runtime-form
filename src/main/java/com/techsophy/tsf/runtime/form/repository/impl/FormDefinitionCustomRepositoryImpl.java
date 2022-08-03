package com.techsophy.tsf.runtime.form.repository.impl;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.repository.FormDefinitionCustomRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@AllArgsConstructor
public class FormDefinitionCustomRepositoryImpl implements FormDefinitionCustomRepository
{
    private final MongoTemplate mongoTemplate;

    @Override
    public List<FormDefinition> findByNameOrId(String idOrNameLike)
    {
        Query query = new Query();
        String searchString = URLDecoder.decode(idOrNameLike, StandardCharsets.UTF_8);
        query.addCriteria(new Criteria().orOperator(Criteria.where(UNDERSCORE_ID).regex(searchString), Criteria.where(FORM_NAME).regex(Pattern.compile(searchString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))));
        return mongoTemplate.find(query, FormDefinition.class);
    }

    @Override
    public List<FormDefinition> findByNameOrIdAndType(String idOrNameLike, String type)
    {
        Query query = new Query();
        String searchString1 = URLDecoder.decode(idOrNameLike, StandardCharsets.UTF_8);
        String searchString2 = URLDecoder.decode(type, StandardCharsets.UTF_8);
        query.addCriteria(new Criteria().andOperator(new Criteria().orOperator(
                        Criteria.where(UNDERSCORE_ID).regex(searchString1),
                        Criteria.where(FORM_NAME).regex(Pattern.compile
                                (searchString1, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))),
                Criteria.where(FORM_TYPE).regex(Pattern.compile
                        (searchString2,Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE))
        ));
        query.with(Sort.by(Sort.Direction.ASC, FORM_NAME));
        return mongoTemplate.find(query, FormDefinition.class);
    }
}
