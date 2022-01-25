/*
 * webhookie - webhook infrastructure that can be incorporated into any microservice or integration architecture.
 * Copyright (C) 2021 Hookie Solutions AB, info@hookiesolutions.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * If your software can interact with users remotely through a computer network, you should also make sure that it provides a way for users to get its source. For example, if your program is a web application, its interface could display a "Source" link that leads users to an archive of the code. There are many ways you could offer source, and different solutions will be better for different programs; see section 13 for the specific requirements.
 *
 * You should also get your employer (if you work as a programmer) or school, if any, to sign a "copyright disclaimer" for the program, if necessary. For more information on this, and how to apply and follow the GNU AGPL, see <https://www.gnu.org/licenses/>.
 */

package com.hookiesolutions.webhookie.webhook.web.response

import com.fasterxml.jackson.annotation.JsonView
import com.hookiesolutions.webhookie.webhook.domain.ConsumerAccess
import com.hookiesolutions.webhookie.webhook.domain.ProviderAccess
import com.hookiesolutions.webhookie.webhook.domain.WebhookApi

/**
 *
 * @author Arthur Kazemi<bidadh@gmail.com>
 * @since 25/1/21 13:36
 */
data class WebhookApiResponse(
  @JsonView(WebhookApiViews.Summary::class)
  val id: String,
  @JsonView(WebhookApiViews.Summary::class)
  val title: String,
  @JsonView(WebhookApiViews.Summary::class)
  val approvalDetails: ApprovalDetails,
  @JsonView(WebhookApiViews.Summary::class)
  val webhookVersion: String,
  @JsonView(WebhookApiViews.Summary::class)
  val description: String?,
  @JsonView(WebhookApiViews.Summary::class)
  val webhooks: List<Webhook>,
  @JsonView(WebhookApiViews.Full::class)
  val raw: String,
  @JsonView(WebhookApiViews.Summary::class)
  val consumerAccess: ConsumerAccess,
  @JsonView(WebhookApiViews.Full::class)
  val consumerGroups: Set<String>,
  @JsonView(WebhookApiViews.Summary::class)
  val providerAccess: ProviderAccess,
  @JsonView(WebhookApiViews.Full::class)
  val providerGroups: Set<String>
) {
  constructor(entity: WebhookApi) : this(
    entity.id!!,
    entity.title,
    ApprovalDetails(entity.approvalDetails.required, entity.approvalDetails.email?.value),
    entity.webhookVersion,
    entity.description,
    entity.webhooks.map { Webhook(Topic(it.topic.name, it.topic.description), it.numberOfSubscriptions) },
    entity.raw,
    entity.consumerAccess,
    entity.consumerIAMGroups,
    entity.providerAccess,
    entity.providerIAMGroups
  )

  @JsonView(WebhookApiViews.Summary::class)
  data class Webhook(
    val topic: Topic,
    val numberOfSubscriptions: Int
  )

  @JsonView(WebhookApiViews.Summary::class)
  data class ApprovalDetails(
    val required: Boolean,
    val email: String?
  )

  @JsonView(WebhookApiViews.Summary::class)
  data class Topic(
    val name: String,
    val description: String?
  )
}
