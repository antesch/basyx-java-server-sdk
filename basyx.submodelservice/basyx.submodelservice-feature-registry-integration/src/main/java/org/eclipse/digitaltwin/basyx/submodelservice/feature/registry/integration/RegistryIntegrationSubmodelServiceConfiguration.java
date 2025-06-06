/*******************************************************************************
 * Copyright (C) 2025 DFKI GmbH (https://www.dfki.de/en/web)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 * 
 ******************************************************************************/
package org.eclipse.digitaltwin.basyx.submodelservice.feature.registry.integration;

import java.util.Collection;
import java.util.List;

import org.eclipse.digitaltwin.basyx.client.internal.authorization.AccessTokenProviderFactory;
import org.eclipse.digitaltwin.basyx.client.internal.authorization.TokenManager;
import org.eclipse.digitaltwin.basyx.client.internal.authorization.grant.AccessTokenProvider;
import org.eclipse.digitaltwin.basyx.client.internal.authorization.grant.GrantType;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.AuthorizedConnectedSubmodelRegistry;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.api.SubmodelRegistryApi;
import org.eclipse.digitaltwin.basyx.submodelregistry.client.mapper.AttributeMapper;
import org.eclipse.digitaltwin.basyx.submodelservice.SubmodelService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration for integrating {@link SubmodelService} with SubmodelRegistry
 * 
 * @author Gerhard Sonnenberg (DFKI GmbH)
 */
@Configuration
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${basyx.submodelservice.feature.registryintegration:}') && !T(org.springframework.util.StringUtils).isEmpty('${basyx.externalurl:}')")
public class RegistryIntegrationSubmodelServiceConfiguration {
	
	@Value("${basyx.submodelservice.feature.registryintegration:#{null}}")
	private String registryBasePath;

	@Value("#{'${basyx.externalurl}'.split(',')}")
	private List<String> submodelRepositoryBaseURLs;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.enabled:false}")
	private boolean isAuthorizationEnabledOnRegistry;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.token-endpoint:#{null}}")
	private String authenticationServerTokenEndpoint;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.grant-type:#{null}}")
	private String grantType;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.client-id:#{null}}")
	private String clientId;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.client-secret:#{null}}")
	private String clientSecret;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.username:#{null}}")
	private String username;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.password:#{null}}")
	private String password;

	@Value("${basyx.submodelservice.feature.registryintegration.authorization.scopes:#{null}}")
	private Collection<String> scopes;
	
	@Bean
	@ConditionalOnMissingBean
	public SubmodelServiceRegistryLink getSubmodelServiceRegistryLink() {
		if (!isAuthorizationEnabledOnRegistry) {
			return new SubmodelServiceRegistryLink(new SubmodelRegistryApi(registryBasePath), submodelRepositoryBaseURLs);
		}
		TokenManager tokenManager = new TokenManager(authenticationServerTokenEndpoint, createAccessTokenProvider());
		return new SubmodelServiceRegistryLink(new AuthorizedConnectedSubmodelRegistry(registryBasePath, tokenManager), submodelRepositoryBaseURLs);
	}
	
	@Bean
	@ConditionalOnMissingBean
	public AttributeMapper getSubmodelAttributeMapper(ObjectMapper objectMapper) {		
		return new AttributeMapper(objectMapper);
	}
	
	private AccessTokenProvider createAccessTokenProvider() {
		AccessTokenProviderFactory factory = new AccessTokenProviderFactory(GrantType.valueOf(grantType), scopes);
		factory.setClientCredentials(clientId, clientSecret);
		factory.setPasswordCredentials(username, password);
		return factory.create();
	}
}
