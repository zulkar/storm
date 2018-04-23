/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package org.apache.storm.task;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.storm.generated.NodeInfo;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.tuple.Fields;

public class WorkerTopologyContext extends GeneralTopologyContext {
    public static final String SHARED_EXECUTOR = "executor";
    Map<String, Object> _userResources;
    Map<String, Object> _defaultResources;
    private Integer _workerPort;
    private List<Integer> _workerTasks;
    private String _codeDir;
    private String _pidDir;
    private AtomicReference<Map<Integer, NodeInfo>> taskToNodePort;
    private String assignmentId;

    public WorkerTopologyContext(
        StormTopology topology,
        Map<String, Object> topoConf,
        Map<Integer, String> taskToComponent,
        Map<String, List<Integer>> componentToSortedTasks,
        Map<String, Map<String, Fields>> componentToStreamToFields,
        String stormId,
        String codeDir,
        String pidDir,
        Integer workerPort,
        List<Integer> workerTasks,
        Map<String, Object> defaultResources,
        Map<String, Object> userResources,
        AtomicReference<Map<Integer, NodeInfo>> taskToNodePort,
        String assignmentId
    ) {
        super(topology, topoConf, taskToComponent, componentToSortedTasks, componentToStreamToFields, stormId);
        _codeDir = codeDir;
        _defaultResources = defaultResources;
        _userResources = userResources;
        try {
            if (pidDir != null) {
                _pidDir = new File(pidDir).getCanonicalPath();
            } else {
                _pidDir = null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not get canonical path for " + _pidDir, e);
        }
        _workerPort = workerPort;
        _workerTasks = workerTasks;
        this.taskToNodePort = taskToNodePort;
        this.assignmentId = assignmentId;

    }

    public WorkerTopologyContext(
        StormTopology topology,
        Map<String, Object> topoConf,
        Map<Integer, String> taskToComponent,
        Map<String, List<Integer>> componentToSortedTasks,
        Map<String, Map<String, Fields>> componentToStreamToFields,
        String stormId,
        String codeDir,
        String pidDir,
        Integer workerPort,
        List<Integer> workerTasks,
        Map<String, Object> defaultResources,
        Map<String, Object> userResources) {
        this(topology, topoConf, taskToComponent, componentToSortedTasks, componentToStreamToFields, stormId,
             codeDir, pidDir, workerPort, workerTasks, defaultResources, userResources, null, null);
    }

    /**
     * Gets all the task ids that are running in this worker process (including the task for this task).
     */
    public List<Integer> getThisWorkerTasks() {
        return _workerTasks;
    }

    public Integer getThisWorkerPort() {
        return _workerPort;
    }

    public String getThisWorkerHost() {
        return assignmentId;
    }

    /**
     * Get a map from task Id to NodePort
     *
     * @return a map from task To NodePort
     */
    public AtomicReference<Map<Integer, NodeInfo>> getTaskToNodePort() {
        return taskToNodePort;
    }

    /**
     * Gets the location of the external resources for this worker on the local filesystem. These external resources typically include bolts
     * implemented in other languages, such as Ruby or Python.
     */
    public String getCodeDir() {
        return _codeDir;
    }

    /**
     * If this task spawns any subprocesses, those subprocesses must immediately write their PID to this directory on the local filesystem
     * to ensure that Storm properly destroys that process when the worker is shutdown.
     */
    public String getPIDDir() {
        return _pidDir;
    }

    public Object getResource(String name) {
        return _userResources.get(name);
    }

    public ExecutorService getSharedExecutor() {
        return (ExecutorService) _defaultResources.get(SHARED_EXECUTOR);
    }
}
