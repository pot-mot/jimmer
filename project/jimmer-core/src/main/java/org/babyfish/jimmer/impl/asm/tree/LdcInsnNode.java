// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.babyfish.jimmer.impl.asm.tree;

import java.util.Map;
import org.babyfish.jimmer.impl.asm.ConstantDynamic;
import org.babyfish.jimmer.impl.asm.Handle;
import org.babyfish.jimmer.impl.asm.MethodVisitor;
import org.babyfish.jimmer.impl.asm.Opcodes;
import org.babyfish.jimmer.impl.asm.Type;

/**
 * A node that represents an LDC instruction.
 *
 * @author Eric Bruneton
 */
public class LdcInsnNode extends AbstractInsnNode {

  /**
   * The constant to be loaded on the stack. This field must be a non null {@link Integer}, a {@link
   * Float}, a {@link Long}, a {@link Double}, a {@link String}, a {@link Type} of OBJECT or ARRAY
   * sort for {@code .class} constants, for classes whose version is 49, a {@link Type} of METHOD
   * sort for MethodType, a {@link Handle} for MethodHandle constants, for classes whose version is
   * 51 or a {@link ConstantDynamic} for a constant dynamic for classes whose version is 55.
   */
  public Object cst;

  /**
   * Constructs a new {@link LdcInsnNode}.
   *
   * @param value the constant to be loaded on the stack. This parameter mist be a non null {@link
   *     Integer}, a {@link Float}, a {@link Long}, a {@link Double}, a {@link String}, a {@link
   *     Type} of OBJECT or ARRAY sort for {@code .class} constants, for classes whose version is
   *     49, a {@link Type} of METHOD sort for MethodType, a {@link Handle} for MethodHandle
   *     constants, for classes whose version is 51 or a {@link ConstantDynamic} for a constant
   *     dynamic for classes whose version is 55.
   */
  public LdcInsnNode(final Object value) {
    super(Opcodes.LDC);
    this.cst = value;
  }

  @Override
  public int getType() {
    return LDC_INSN;
  }

  @Override
  public void accept(final MethodVisitor methodVisitor) {
    methodVisitor.visitLdcInsn(cst);
    acceptAnnotations(methodVisitor);
  }

  @Override
  public AbstractInsnNode clone(final Map<LabelNode, LabelNode> clonedLabels) {
    return new LdcInsnNode(cst).cloneAnnotations(this);
  }
}
