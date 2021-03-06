/*
 * Copyright 2017 Axway Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axway.ats.testexplorer.model.db;

import java.sql.SQLException;
import java.util.List;

import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
import com.axway.ats.log.autodb.model.IDbReadAccess;
import com.axway.ats.testexplorer.pages.model.TableColumn;
import com.axway.ats.testexplorer.pages.testcasesByGroups.TestcaseInfoPerGroupStorage;

public interface TestExplorerDbReadAccessInterface extends IDbReadAccess {

    public MessageFilterDetails getMessageFilterDetails(
                                                         String whereClause ) throws DatabaseAccessException;

    public List<TestcaseCompareDetails> getTestcaseToCompareDetails(
                                                                     String whereClause ) throws DatabaseAccessException;

    public List<QueueCompareDetails> getQueuesToCompareDetails(
                                                                List<String> runIds ) throws DatabaseAccessException;

    public PageNavigation getNavigationForSuite(
                                                 String runId ) throws DatabaseAccessException;

    public PageNavigation getNavigationForScenario(
                                                    String suiteId ) throws DatabaseAccessException;

    public PageNavigation getNavigationForTestcases(
                                                     String scenarioId ) throws DatabaseAccessException;

    public PageNavigation getNavigationForTestcase(
                                                    String testcaseId ) throws DatabaseAccessException;

    public String getMachineInformation(
                                         int machineId ) throws DatabaseAccessException;

    public String getSpecificTestcaseId(
                                         String currentTestcaseId,
                                         String runName,
                                         String suiteName,
                                         String scenarioName,
                                         String testName,
                                         boolean getNext,
                                         boolean getLast ) throws DatabaseAccessException;

    public List<TableColumn> getTableColumnDefinition() throws DatabaseAccessException, SQLException;

    public List<String[]> getProductAndVersionNames() throws DatabaseAccessException;

    public TestcaseInfoPerGroupStorage getTestcaseInfoPerGroupStorage(
                                                                       String whereClause ) throws DatabaseAccessException;

    public List<String> getAllProductNames(
                                            String whereClause ) throws DatabaseAccessException;

    public List<String> getAllVersionNames(
                                            String whereClause ) throws DatabaseAccessException;

    public List<String> getAllGroupNames(
                                          String whereClause ) throws DatabaseAccessException;

    public List<String> getAllBuildTypes(
                                          String whereClause ) throws DatabaseAccessException;
}
