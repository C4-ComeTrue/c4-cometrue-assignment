package org.c4marathon.assignment.global.common;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CouponElParser {

	public static Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
		SpelExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext();

		for (int i = 0; i < parameterNames.length; i++) {
			context.setVariable(parameterNames[i], args[i]);
		}

		return parser.parseExpression(key).getValue(context, Object.class);
	}
}
