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
package com.axway.ats.testexplorer.pages.runsByTypeDashboard.home;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;

import com.axway.ats.log.autodb.entities.Run;
import com.axway.ats.log.autodb.exceptions.DatabaseAccessException;
import com.axway.ats.testexplorer.model.TestExplorerSession;

public class DashboardHomeUtils {

    public List<String> initJsonData(
                                      List<String[]> productAndVersionNames,
                                      List<String> buildTypes ) throws DatabaseAccessException {

        List<String> jsonData = new ArrayList<String>( 1 );

        if( productAndVersionNames == null ) {
            jsonData.add( "[]" );//chartData
            jsonData.add( "[]" );//runsData
            jsonData.add( "[]" );//statusData
            return jsonData;
        }

        /*
        [ -> all chartData
            [ -> single productAndVersionChartData
            
                { -> single buildTypeChartData
                
                },
                {
                
                }
            ]
        ]
        */

        StringBuilder chartData = new StringBuilder( "[" );
        StringBuilder runsData = new StringBuilder( "[" );
        StringBuilder statusData = new StringBuilder( "[" );

        for( String[] productAndVersion : productAndVersionNames ) {

            chartData.append( "[" );
            runsData.append( "[" );
            statusData.append( "[" );

            final String whereClause = "WHERE productName = '" + productAndVersion[0]
                                       + "' AND versionName = '" + productAndVersion[1] + "'";

            for( String buildType : buildTypes ) {
                String currentBuildWhereClause = new String( whereClause
                                                             + " AND runId IN (SELECT runId FROM tRunMetainfo WHERE name='type' AND value='"
                                                             + ( buildType ) + "')" );
                List<Run> runs = getRuns( currentBuildWhereClause );
                if(runs == null || runs.size() == 0){
                    continue;
                }
                addData( runs,
                          chartData,
                          runsData,
                          statusData,
                          productAndVersion[0],
                          productAndVersion[1],
                          buildType );
            }

            String unspecifiedRunsWhereClause = whereClause
                                                + " AND runId NOT IN (SELECT runId from tRunMetainfo WHERE name='type')";
            List<Run> unspecifiedRuns = getRuns( unspecifiedRunsWhereClause );
            addData( unspecifiedRuns,
                      chartData,
                      runsData,
                      statusData,
                      productAndVersion[0],
                      productAndVersion[1],
                      "unspecified" );

            chartData.append( "]," );
            runsData.append( "]," );
            statusData.append( "]," );

        }

        chartData.append( "]" );
        runsData.append( "]" );
        statusData.append( "]" );

        jsonData.add( chartData.toString() );
        jsonData.add( runsData.toString() );
        jsonData.add( statusData.toString() );

        return jsonData;
    }

    private List<Run> getRuns(
                               String whereClause ) throws DatabaseAccessException {

        TestExplorerSession session = ( TestExplorerSession ) Session.get();

        List<Run> runs = session.getDbReadConnection().getRuns( 0,
                                                                session.getDbReadConnection()
                                                                       .getRunsCount( whereClause ),
                                                                whereClause,
                                                                "dateStart",
                                                                false );

        return runs;
    }

    private String initRunJsonDatum(
                                     Run run ) {

        StringBuilder sb = new StringBuilder();

        sb.append( "{" )
          .append( "\"_id\":\"" + run.runName + "\"," )
          .append( "\"Build\":\"" + run.buildName + "\"," )
          .append( "\"Status\":\"" + "OK" + "\"," )
          .append( "\"Result\":\"" + ( ( run.testcasesFailed >= 1 )
                                                                   ? "FAIL"
                                                                   : "PASS" ) + "\"" )
          .append( "}," );

        return sb.toString();

    }

    private String initChartJsonDatum(
                                       String productName,
                                       String versionName,
                                       String type,
                                       int totalIterations,
                                       double passingRate ) {

        StringBuilder sb = new StringBuilder();

        sb.append( "{" )
          .append( "\"product\":\"" + ( productName ) + "\"," )
          .append( "\"version\":\"" + ( versionName ) + "\"," )
          .append( "\"type\":\"" + ( type ) + "\"," )
          .append( "\"totalIterations\":\"" + "Total Iterations: " + totalIterations + "\"," )
          .append( "\"passingRate\":\"" + ( ( int ) Math.floor( passingRate ) + "% Passing" ) + "\"" )
          .append( "}," );

        return sb.toString();
    }

    private void addData(
                           List<Run> runs,
                           StringBuilder chartData,
                           StringBuilder runsData,
                           StringBuilder statusData,
                           String productName,
                           String versionName,
                           String type ) {

        StringBuilder runsBuilder = new StringBuilder( "[" );

        int totalRuns = 0;
        int passedRuns = 0;
        double passingRate = 0;

        if( runs != null && runs.size() > 0 ) {

            for( Run run : runs ) {
                if( run.testcasesFailed == 0 && run.testcasesSkipped == 0) {
                    passedRuns++;
                }
                runsBuilder.append( initRunJsonDatum( run ) );
            }

            totalRuns = runs.size();

            passingRate = ( ( double ) ( passedRuns ) / totalRuns ) * 100.0;

        }

        runsBuilder.append( "]," );

        passingRate = ( ( double ) ( passedRuns ) / totalRuns ) * 100.0;
        chartData.append( initChartJsonDatum( productName, versionName, type, totalRuns, passingRate ) );
        runsData.append( runsBuilder.toString() );
        statusData.append( initStatusData( runs ) );

    }

    private String initStatusData(
                                   List<Run> runs ) {

        if( runs == null || runs.size() < 1 ) {
            return "{},";
        }

        return "{'Last Run Status':'" + ( ( runs.get( 0 ).testcasesFailed >= 1 )
                                                                                ? "FAIL"
                                                                                : "PASS" ) + "'},";
    }

    public void callJavaScript(
                                Object source,
                                List<String> jsonData ) {
        
        if( source == null || jsonData == null ) {
            return;
        }
        
        String script = ";setChartData(" + jsonData.get( 0 ) + ")" + ";setRunsData(" + jsonData.get( 1 )
                        + ");setStatusData(" + jsonData.get( 2 ) + ")" + ";resize();";

        if( source instanceof AjaxRequestTarget ) {
            ( ( AjaxRequestTarget ) source ).appendJavaScript( script );
        } else if( source instanceof IHeaderResponse ) {
            ( ( IHeaderResponse ) source ).render( OnLoadHeaderItem.forScript( script ) );
        } else {
            // maybe it is good to throw some Exception here
        }
    }

}
