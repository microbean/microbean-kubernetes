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

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import io.fabric8.kubernetes.api.model.Pod;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;

import org.junit.Test;

public class TestPods {

  public TestPods() {
    super();
  }

  @Test
  public void testGetFirstReadyPod() throws IOException {
    final DefaultKubernetesClient client = new DefaultKubernetesClient();
    final Map<String, String> labels = new HashMap<>();
    labels.put("app", "helm");
    final Pod pod = Pods.getFirstReadyPod(client.pods().inNamespace("kube-system").withLabels(labels));
    System.out.println("pod: " + pod);
  }
  
}
