package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.enumeration.OriginSourceType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;

public class ProjectViewWithoutDescription extends BlackDuckView {
    public java.util.List<ProjectCloneCategoriesType> cloneCategories;
    public java.util.Date createdAt;
    public String createdBy;
    public String createdByUser;
    public String name;
    public Boolean projectLevelAdjustments;
    public String projectOwner;
    public Integer projectTier;
    public OriginSourceType source;
    public java.util.Date updatedAt;
    public String updatedBy;
    public String updatedByUser;

}
