package com.techsophy.tsf.runtime.form.config;

import com.techsophy.tsf.runtime.form.entity.FormDefinition;
import com.techsophy.tsf.runtime.form.repository.FormDefinitionRepository;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import static com.techsophy.tsf.runtime.form.constants.FormModelerConstants.*;

@ChangeUnit(id=TP_APP_RUNTIME_FORM, order =ORDER_1,systemVersion=SYSTEM_VERSION_1)
@RequiredArgsConstructor
public class DatabaseChangeLog
{
    private final FormDefinitionRepository formDefinitionRepository;
    List<FormDefinition> formDefinitionList;

    @Execution
    public void changeSetFormDefinition()
    {
        formDefinitionList =formDefinitionRepository.findAll();
        List<FormDefinition> modificationFormList=formDefinitionList;
        modificationFormList.forEach(
                formDefinition ->
                {
                    if(String.valueOf(formDefinition.getComponents()).equals(NULL))
                    {
                        formDefinition.setComponents(Map.of());
                    }
                    if(String.valueOf(formDefinition.getProperties()).equals(NULL))
                    {
                        formDefinition.setProperties(Map.of());
                    }
                    if(String.valueOf(formDefinition.getIsDefault()).equals(NULL))
                    {
                        formDefinition.setIsDefault(false);
                    }
                });
        formDefinitionRepository.saveAll(modificationFormList);
    }

    @RollbackExecution
    public void rollback()
    {
     formDefinitionRepository.saveAll(formDefinitionList);
    }
}
