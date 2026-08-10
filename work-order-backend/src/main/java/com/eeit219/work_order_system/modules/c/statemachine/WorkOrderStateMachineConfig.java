package com.eeit219.work_order_system.modules.c.statemachine;

import java.util.EnumSet;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class WorkOrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<WorkOrderState, WorkOrderEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<WorkOrderState, WorkOrderEvent> states)throws Exception{
        
        states.withStates()
                .initial(WorkOrderState.PENDING_REVIEW)
                .states(EnumSet.allOf(WorkOrderState.class))
                .end(WorkOrderState.COMPLETED)
                .end(WorkOrderState.CANCELLED);

    }
    @Override
    public void configure(StateMachineTransitionConfigurer<WorkOrderState, WorkOrderEvent> transitions) throws Exception {
              transitions
                // 待審核 -拒絕-> 已取消
                .withExternal()
                    .source(WorkOrderState.PENDING_REVIEW)
                    .target(WorkOrderState.CANCELLED)
                    .event(WorkOrderEvent.REJECT)
                    .and()
                // 待審核 -接受-> 進行中    
                .withExternal()
                    .source(WorkOrderState.PENDING_REVIEW)
                    .target(WorkOrderState.IN_PROGRESS)
                    .event(WorkOrderEvent.ACCEPT)
                    .and()
                // 進行中 -拒絕-> 待審核   
                .withExternal()
                    .source(WorkOrderState.IN_PROGRESS)
                    .target(WorkOrderState.PENDING_REVIEW)
                    .event(WorkOrderEvent.REJECT)
                    .and()  
                // 進行中 -完成-> 使用者驗收
                .withExternal()
                    .source(WorkOrderState.IN_PROGRESS)
                    .target(WorkOrderState.PENDING_USER_ACCEPTANCE)
                    .event(WorkOrderEvent.ACCEPT)
                    .and()
                // 使用者驗收 -接受-> 管理員驗收
                .withExternal()
                    .source(WorkOrderState.PENDING_USER_ACCEPTANCE)
                    .target(WorkOrderState.PENDING_ADMIN_ACCEPTANCE)
                    .event(WorkOrderEvent.ACCEPT)
                    .and()
                // 管理員驗收 -拒絕-> 進行中
                .withExternal()
                    .source(WorkOrderState.PENDING_ADMIN_ACCEPTANCE)
                    .target(WorkOrderState.IN_PROGRESS)
                    .event(WorkOrderEvent.REJECT)
                    .and()
                // 管理員驗收 -接受-> 完成
                .withExternal()
                    .source(WorkOrderState.PENDING_ADMIN_ACCEPTANCE)
                    .target(WorkOrderState.COMPLETED)
                    .event(WorkOrderEvent.ACCEPT);         

    }

}
