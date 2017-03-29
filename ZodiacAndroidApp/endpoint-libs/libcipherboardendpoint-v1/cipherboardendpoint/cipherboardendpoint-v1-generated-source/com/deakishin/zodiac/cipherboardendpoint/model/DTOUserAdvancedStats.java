/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://github.com/google/apis-client-generator/
 * (build: 2016-10-17 16:43:55 UTC)
 * on 2016-12-06 at 13:33:45 UTC 
 * Modify at your own risk.
 */

package com.deakishin.zodiac.cipherboardendpoint.model;

/**
 * Model definition for DTOUserAdvancedStats.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the cipherboardendpoint. For a detailed explanation see:
 * <a href="https://developers.google.com/api-client-library/java/google-http-java-client/json">https://developers.google.com/api-client-library/java/google-http-java-client/json</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class DTOUserAdvancedStats extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer createdCount;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Float points;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer rank;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer solvedCount;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer solvedFirstCount;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getCreatedCount() {
    return createdCount;
  }

  /**
   * @param createdCount createdCount or {@code null} for none
   */
  public DTOUserAdvancedStats setCreatedCount(java.lang.Integer createdCount) {
    this.createdCount = createdCount;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Float getPoints() {
    return points;
  }

  /**
   * @param points points or {@code null} for none
   */
  public DTOUserAdvancedStats setPoints(java.lang.Float points) {
    this.points = points;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getRank() {
    return rank;
  }

  /**
   * @param rank rank or {@code null} for none
   */
  public DTOUserAdvancedStats setRank(java.lang.Integer rank) {
    this.rank = rank;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getSolvedCount() {
    return solvedCount;
  }

  /**
   * @param solvedCount solvedCount or {@code null} for none
   */
  public DTOUserAdvancedStats setSolvedCount(java.lang.Integer solvedCount) {
    this.solvedCount = solvedCount;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getSolvedFirstCount() {
    return solvedFirstCount;
  }

  /**
   * @param solvedFirstCount solvedFirstCount or {@code null} for none
   */
  public DTOUserAdvancedStats setSolvedFirstCount(java.lang.Integer solvedFirstCount) {
    this.solvedFirstCount = solvedFirstCount;
    return this;
  }

  @Override
  public DTOUserAdvancedStats set(String fieldName, Object value) {
    return (DTOUserAdvancedStats) super.set(fieldName, value);
  }

  @Override
  public DTOUserAdvancedStats clone() {
    return (DTOUserAdvancedStats) super.clone();
  }

}