package org.flowable.cmmn.engine.impl.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.flowable.cmmn.engine.impl.persistence.entity.CaseDefinitionEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.CaseInstanceEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.CmmnDeploymentEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.CmmnResourceEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.HistoricCaseInstanceEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.HistoricMilestoneInstanceEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.MilestoneInstanceEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.PlanItemInstanceEntityImpl;
import org.flowable.cmmn.engine.impl.persistence.entity.SentryPartInstanceEntityImpl;
import org.flowable.engine.common.impl.persistence.entity.Entity;
import org.flowable.identitylink.service.impl.persistence.entity.HistoricIdentityLinkEntityImpl;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.HistoricVariableInstanceEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.VariableByteArrayEntityImpl;
import org.flowable.variable.service.impl.persistence.entity.VariableInstanceEntityImpl;

public class EntityDependencyOrder {

    public static List<Class<? extends Entity>> DELETE_ORDER = new ArrayList<>();

    public static List<Class<? extends Entity>> INSERT_ORDER = new ArrayList<>();

    static {
        DELETE_ORDER.add(HistoricIdentityLinkEntityImpl.class);
        DELETE_ORDER.add(HistoricMilestoneInstanceEntityImpl.class);
        DELETE_ORDER.add(HistoricCaseInstanceEntityImpl.class);
        DELETE_ORDER.add(VariableInstanceEntityImpl.class);
        DELETE_ORDER.add(VariableByteArrayEntityImpl.class);
        DELETE_ORDER.add(HistoricVariableInstanceEntityImpl.class);
        DELETE_ORDER.add(IdentityLinkEntityImpl.class);
<<<<<<< MINE
        DELETE_ORDER.add(HistoricIdentityLinkEntityImpl.class);
=======
>>>>>>> YOURS
        DELETE_ORDER.add(MilestoneInstanceEntityImpl.class);
        DELETE_ORDER.add(SentryPartInstanceEntityImpl.class);
        DELETE_ORDER.add(PlanItemInstanceEntityImpl.class);
        DELETE_ORDER.add(CaseInstanceEntityImpl.class);
        DELETE_ORDER.add(CaseDefinitionEntityImpl.class);
        DELETE_ORDER.add(CmmnResourceEntityImpl.class);
        DELETE_ORDER.add(CmmnDeploymentEntityImpl.class);
        INSERT_ORDER = new ArrayList<>(DELETE_ORDER);
        Collections.reverse(INSERT_ORDER);
    }
}
