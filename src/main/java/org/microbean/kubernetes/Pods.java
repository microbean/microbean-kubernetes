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

import java.io.Closeable; // for javadoc only

import java.net.MalformedURLException;
import java.net.URL;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodStatus;

import io.fabric8.kubernetes.client.LocalPortForward;

import io.fabric8.kubernetes.client.dsl.Listable;

import io.fabric8.kubernetes.client.dsl.base.OperationSupport;

import io.fabric8.kubernetes.client.dsl.internal.PortForwarderWebsocket;

import okhttp3.OkHttpClient;

/**
 * A utility class that helps with operations concerning {@link Pod}s.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see #forwardPort(OkHttpClient, Listable, int)
 *
 * @see #isReady(Pod)
 *
 * @see Pod
 */
public final class Pods {


  /*
   * Constructors.
   */

  /**
   * Creates a new {@link Pods} instance.
   */
  private Pods() {
    super();
  }


  /*
   * Static methods.
   */
  

  /**
   * Forwards an arbitrary local port to the supplied {@code
   * remotePort} on the {@linkplain #getFirstReadyPod(Listable) first
   * ready <code>Pod</code>} in the supplied {@link Listable} using
   * the supplied {@link OkHttpClient}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param httpClient an {@link OkHttpClient} used to communicate
   * with Kubernetes; may be {@code null} in which case {@code null}
   * will be returned
   *
   * @param pods a {@link Listable} of {@link PodList}s; <strong>must
   * also be an instance of {@link OperationSupport}</strong> or
   * {@code null} will be returned
   *
   * @param remotePort the port on the {@link Pod} that is ultimately
   * located to which traffic should be forwarded
   *
   * @return a {@link LocalPortForward} representing the port
   * forwarding operation, or {@code null}; must be {@linkplain
   * Closeable#close() closed} if non-{@code null}
   *
   * @exception MalformedURLException if a {@link URL} to the
   * Kubernetes resource representing the {@link Pod} in question
   * could not be constructed
   *
   * @see PortForwarderWebsocket#PortForwarderWebsocket(OkHttpClient)
   *
   * @see <a
   * href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">Pod
   * conditions documentation</a>
   */
  public static final LocalPortForward forwardPort(final OkHttpClient httpClient, final Listable<? extends PodList> pods, final int remotePort) throws MalformedURLException {
    LocalPortForward returnValue = null;
    if (httpClient != null && pods instanceof OperationSupport) {
      final String urlBase = ((OperationSupport)pods).getNamespacedUrl().toExternalForm();
      assert urlBase != null;
      final Pod readyPod = getFirstReadyPod(pods);
      if (readyPod != null) {
        final String name = readyPod.getMetadata().getName();
        assert name != null;
        final URL url = new URL(new StringBuilder(urlBase).append("/").append(name).toString());
        returnValue = new PortForwarderWebsocket(httpClient).forward(url, remotePort);
      }
    }
    return returnValue;
  }

  /**
   * Returns {@link Boolean#TRUE} if the supplied {@link Pod} is known
   * to be ready, {@link Boolean#FALSE} if it is known to be not ready
   * and {@code null} if its readiness status is unknown.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param pod the {@link Pod} to check; may be {@code null} in which
   * case {@code null} will be returned
   *
   * @return {@link Boolean#TRUE} if the supplied {@link Pod} is known
   * to be ready, {@link Boolean#FALSE} if it is known to be not ready
   * and {@code null} if its readiness status is unknown
   *
   * @see #isReady(PodStatus)
   *
   * @see <a
   * href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">Pod
   * conditions documentation</a>
   */
  public static final Boolean isReady(final Pod pod) {
    Boolean returnValue = null;
    if (pod != null) {
      returnValue = isReady(pod.getStatus());
    }
    return returnValue;
  }

  /**
   * Returns {@link Boolean#TRUE} if the supplied {@link PodStatus} is
   * known to be ready, {@link Boolean#FALSE} if it is known to be not
   * ready and {@code null} if its readiness status is unknown.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param podStatus the {@link PodStatus} to check; may be {@code
   * null} in which case {@code null} will be returned
   *
   * @return {@link Boolean#TRUE} if the supplied {@link PodStatus} is
   * known to be ready, {@link Boolean#FALSE} if it is known to be not
   * ready and {@code null} if its readiness status is unknown
   *
   * @see #isReady(Iterable)
   *
   * @see <a
   * href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">Pod
   * conditions documentation</a>
   */
  public static final Boolean isReady(final PodStatus podStatus) {
    Boolean returnValue = null;
    if (podStatus != null) {
      returnValue = isReady(podStatus.getConditions());
    }
    return returnValue;
  }

  /**
   * Returns {@link Boolean#TRUE} if the supplied {@link Iterable} of
   * {@link PodCondition}s contains a {@link PodCondition} whose
   * {@link PodCondition#getType()} method returns {@code Ready} and
   * whose {@link PodCondition#getStatus()} method returns {@code
   * True}.
   *
   * <p>If the supplied {@link Iterable} of
   * {@link PodCondition}s <em>also</em> contains a {@link PodCondition} whose
   * {@link PodCondition#getType()} method returns {@code Ready} and
   * whose {@link PodCondition#getStatus()} method returns {@code
   * False}, then {@code null} is returned.</p>
   *
   * <p>If instead the supplied {@link Iterable} of
   * {@link PodCondition}s contains a {@link PodCondition} whose
   * {@link PodCondition#getType()} method returns {@code Ready} and
   * whose {@link PodCondition#getStatus()} method returns {@code
   * False} and does not <em>also</em> contains a {@link PodCondition} whose
   * {@link PodCondition#getType()} method returns {@code Ready} and
   * whose {@link PodCondition#getStatus()} method returns {@code
   * True}, then {@link Boolean#FALSE} is returned.</p>
   *
   * <p>{@code null} is returned in all other cases.</p>
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param podConditions an {@link Iterable} of {@link
   * PodCondition}s; may be {@code null} in which case {@code null}
   * will be returned
   *
   * @return {@link Boolean#TRUE} if the supplied {@link Iterable} of
   * {@link PodCondition}s represents a state of affairs known to
   * represent the readiness of the {@link PodStatus} they describe;
   * {@link Boolean#FALSE} if the supplied {@link Iterable} of {@link
   * PodCondition}s represents a state of affairs known to represent
   * the unreadiness of the {@link PodStatus} they describe, or {@code
   * null} in all other cases
   *
   * @see <a
   * href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">Pod
   * conditions documentation</a>
   */
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
              if (Boolean.FALSE.equals(returnValue)) {
                returnValue = null;
              } else {
                returnValue = Boolean.TRUE;
              }
              break;
            case "False":
              if (Boolean.TRUE.equals(returnValue)) {
                returnValue = null;
              } else {
                returnValue = Boolean.FALSE;
              }
              break;
            case "Unknown":
              returnValue = null;
              break;
            default:
              throw new IllegalStateException("Unexpected value for PodCondition of type Ready: " + conditionStatus);
            }
          }
        }
      }
    }
    return returnValue;
  }

  /**
   * Returns the first {@link Pod} encountered in the supplied {@link
   * Iterable} of {@link Pod}s for which the {@link #isReady(Pod)}
   * method returns {@link Boolean#TRUE}, or {@code null}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param pods an {@link Iterable} of {@link Pod}s to check; may be
   * {@code null} in which case {@code null} will be returned
   *
   * @return a {@link Pod} known to be ready, or {@code null}
   *
   * @see #isReady(Pod)
   *
   * @see <a
   * href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">Pod
   * conditions documentation</a>
   */
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

  /**
   * Returns the first {@link Pod} encountered in the supplied {@link
   * PodList} of {@link Pod}s for which the {@link #isReady(Pod)}
   * method returns {@link Boolean#TRUE}, or {@code null}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param podList a {@link PodList} of {@link Pod}s to check; may be
   * {@code null} in which case {@code null} will be returned
   *
   * @return a {@link Pod} known to be ready, or {@code null}
   *
   * @see #getFirstReadyPod(Iterable)
   *
   * @see #isReady(Pod)
   *
   * @see <a
   * href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">Pod
   * conditions documentation</a>
   */
  public static final Pod getFirstReadyPod(final PodList podList) {
    Pod returnValue = null;
    if (podList != null) {
      returnValue = getFirstReadyPod(podList.getItems());
    }
    return returnValue;
  }

  /**
   * Returns the first {@link Pod} reachable from the supplied {@link
   * Listable} of {@link PodList}s for which the {@link #isReady(Pod)}
   * method returns {@link Boolean#TRUE}, or {@code null}.
   *
   * <p>This method may return {@code null}.</p>
   *
   * @param podsResource a {@link Listable} of {@link PodList}s
   * representing {@link Pod}s to check; may be {@code null} in which
   * case {@code null} will be returned
   *
   * @return a {@link Pod} known to be ready, or {@code null}
   *
   * @see #getFirstReadyPod(PodList)
   *
   * @see #isReady(Pod)
   *
   * @see <a
   * href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">Pod
   * conditions documentation</a>
   */
  public static final Pod getFirstReadyPod(final Listable<? extends PodList> podsResource) {
    Pod returnValue = null;
    if (podsResource != null) {
      returnValue = getFirstReadyPod(podsResource.list());
    }
    return returnValue;
  }
  
}
