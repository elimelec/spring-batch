/*
 * Copyright 2006-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.batch.core.step.item;

import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.item.Chunk;

/**
 * Interface for providing {@link org.springframework.batch.item.Chunk}s to be processed,
 * used by the {@link ChunkOrientedTasklet}
 *
 * @since 2.0
 * @see ChunkOrientedTasklet
 */
public interface ChunkProvider<T> {

	Chunk<T> provide(StepContribution contribution) throws Exception;

	void postProcess(StepContribution contribution, Chunk<T> chunk);

}
