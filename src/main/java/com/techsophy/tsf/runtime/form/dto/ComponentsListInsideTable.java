package com.techsophy.tsf.runtime.form.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComponentsListInsideTable
{
   List<Component> components;
}
