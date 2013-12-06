package com.taobao.tddl.repo.mysql.spi;

import java.util.Map;

import javax.sql.DataSource;

import com.taobao.tddl.executor.ExecutorContext;
import com.taobao.tddl.executor.common.TopologyHandler;
import com.taobao.tddl.executor.spi.IDataSourceGetter;
import com.taobao.tddl.executor.spi.IGroupExecutor;
import com.taobao.tddl.optimizer.config.Group;

public class DatasourceMySQLImplement implements IDataSourceGetter {

    public static DataSource getDatasourceByGroupNode(TopologyHandler topology, String groupNode) {

        Map<String, IGroupExecutor> executorMap = topology.getExecutorMap();
        if ("undecided".equals(groupNode)) {
            return null;
        }
        IGroupExecutor matrixExecutor = executorMap.get(groupNode);

        if (matrixExecutor == null) return null;
        /*
         * 这里做个hack吧。 原因是在构造topologic的时候，默认全部使用mysql作为type了。
         * 这样就造成所有的Datasource都是由RemotingExecutor
         * com.taobao.ustore.jdbc.mysql.My_Reponsitory.buildRemoting(Group
         * group) 创建的，type都是mysql.
         * 但实际上这里的type需要根据实际的DataSource来决定是个oracle还是个mysql.
         * 如果是oracle那么type需要改成oracle..
         */
        Group.GroupType type = matrixExecutor.getGroupInfo().getType();
        DataSource ds = (DataSource) matrixExecutor.getRemotingExecutableObject();

        if (isNotValidateNode(type)) {
            throw new IllegalArgumentException("target node is not a validated Jdbc node");
        }

        if (ds == null) {
            throw new IllegalArgumentException("can't find ds by group name ");
        }
        return ds;
    }

    private static boolean isNotValidateNode(Group.GroupType type) {

        return !Group.GroupType.MYSQL_JDBC.equals(type) && !Group.GroupType.TDHS_CLIENT.equals(type)
               && !Group.GroupType.ORACLE_JDBC.equals(type);
    }

    @Override
    public DataSource getDataSource(String group) {
        return this.getDatasourceByGroupNode(ExecutorContext.getContext().getTopologyHandler(), group);
    }

}