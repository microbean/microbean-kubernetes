/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.kubernetes;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;

import io.fabric8.kubernetes.client.dsl.Listable;

public class Pods {

  public static final Boolean isReady(final Pod pod) {
    Boolean returnValue = null;
    if (pod != null) {
      returnValue = isReady(pod.getStatus());
    }
    return returnValue;
  }

  public static final Boolean isReady(final PodStatus podStatus) {
    Boolean returnValue = null;
    if (podStatus != null) {
      returnValue = isReady(podStatus.getConditions());
    }
    return returnValue;
  }
  
  public static final Boolean isReady(final Iterable<? extends PodCondition> podConditions) {
    Boolean returnValue = null;
    if (podConditions != null) {
      for (final PodCondition condition : podConditions) {
        if (condition != null) {
          final String conditionType = condition.getType();
          // See https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions.
          if ("Ready".equals(conditionType)) {
            final String conditionStatus = condition.getStatus();
            switch (conditionStatus) {
            case "True":
              returnValue = Boolean.TRUE;
              break;
            case "False":
              returnValue = Boolean.FALSE;
              break;
            case "Unknown":
              returnValue = null;
              break;
            default:
              throw new IllegalStateException("Unexpected value for PodCondition of type Ready: " + conditionStatus);
            }
            break;
          }
        }
      }
    }
    return returnValue;
  }

  public static final Pod getFirstReadyPod(final Iterable<? extends Pod> pods) {
    Pod returnValue = null;
    if (pods != null) {
      for (final Pod pod : pods) {
        if (Boolean.TRUE.equals(isReady(pod))) {
          returnValue = pod;
          break;
        }
      }
    }
    return returnValue;
  }
  
  public static final Pod getFirstReadyPod(final PodList podList) {
    Pod returnValue = null;
    if (podList != null) {
      returnValue = getFirstReadyPod(podList.getItems());
    }
    return returnValue;
  }
  
  public static final Pod getFirstReadyPod(final Listable<? extends PodList> podsResource) {
    Pod returnValue = null;
    if (podsResource != null) {
      returnValue = getFirstReadyPod(podsResource.list());
    }
    return returnValue;
  }
  
}
