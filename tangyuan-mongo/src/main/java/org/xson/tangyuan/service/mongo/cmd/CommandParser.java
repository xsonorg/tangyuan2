package org.xson.tangyuan.service.mongo.cmd;

import java.util.ArrayList;
import java.util.List;

import org.xson.tangyuan.TangYuanException;

public class CommandParser {

	private static char escapeFlag = '\\';

	private int findMatchedChar(int start, String context, char startChar, char endChar) {
		char chr;
		boolean stringMode = false;
		boolean stringModeSingleQuotes = false;
		boolean stringModeDoubleQuotes = false;
		int count = 0;
		for (int i = start + 1; i < context.length(); i++) {
			chr = context.charAt(i);
			switch (chr) {
			case '\'':
				if (stringMode) {
					if (stringModeSingleQuotes && (0 == i || escapeFlag != context.charAt(i - 1))) {
						stringModeSingleQuotes = false;
						stringMode = false;
					}
				} else {
					if (0 == i || escapeFlag != context.charAt(i - 1)) {
						stringModeSingleQuotes = true;
						stringMode = true;
					}
				}
				break;
			case '"':
				if (stringMode) {
					if (stringModeDoubleQuotes && (0 == i || escapeFlag != context.charAt(i - 1))) {
						stringModeDoubleQuotes = false;
						stringMode = false;
					}
				} else {
					if (0 == i || '\\' != context.charAt(i - 1)) {
						stringModeDoubleQuotes = true;
						stringMode = true;
					}
				}
				break;
			default:
				if (stringMode) {
					break;
				}
				if (chr == startChar && (0 == i || escapeFlag != context.charAt(i - 1))) {
					count++;
				} else if (chr == endChar && (0 == i || escapeFlag != context.charAt(i - 1))) {
					if (count == 0) {
						return i;
					} else {
						count--;
					}
				}
			}
		}
		return -1;
	}

	private String cleanCommand(String context) {
		String command = context.trim();
		int endIndex = command.lastIndexOf(")");
		if (endIndex < 0) {
			throw new TangYuanException("Invalid mongo command: " + context);
		}
		if (endIndex == (command.length() - 1)) {
			return command;
		}
		return command.substring(0, endIndex + 1);
	}

	private List<String> parseParameter(String context) {
		List<String> parameters = new ArrayList<String>();
		int index = 0;
		int length = context.length();
		StringBuilder parameterBuilder = new StringBuilder();
		boolean findParameter = false;
		char chr;
		while (index < length) {
			chr = context.charAt(index);
			if (chr == ' ' || chr == '\t') {
				if (!findParameter) {// 空白字符，忽略
					index++;
					continue;
				}
				parameterBuilder.append(chr);
			} else if (chr == '{' || chr == '[' || chr == '(') {
				int endIndex = -1;
				if (chr == '{') {
					endIndex = findMatchedChar(index, context, '{', '}');
				} else if (chr == '[') {
					endIndex = findMatchedChar(index, context, '[', ']');
				} else if (chr == '(') {
					endIndex = findMatchedChar(index, context, '(', ')');
				}
				if (endIndex < 0) {
					throw new TangYuanException("Invalid mongo parameter: " + context);
				}

				parameterBuilder.append(context, index, endIndex + 1);
				index = endIndex;

				if (!findParameter) {
					findParameter = true;
				}
			} else if (chr == ',') {
				if (!findParameter) {
					throw new TangYuanException("Invalid mongo parameter: " + context);
				}
				// 当前参数结束
				parameters.add(parameterBuilder.toString().trim());
				parameterBuilder.setLength(0);
				findParameter = false;
			} else {
				parameterBuilder.append(chr);
				if (!findParameter) {
					findParameter = true;
				}
			}
			index++;
		}

		if (parameterBuilder.length() > 0) {
			parameters.add(parameterBuilder.toString().trim());
		}

		return parameters;
	}

	public List<CommandVo> parse(String context) {
		context = cleanCommand(context);
		List<CommandVo> commandVoList = new ArrayList<CommandVo>();
		int index = 0;
		int length = context.length();
		StringBuilder actionBuilder = new StringBuilder();
		boolean findAction = false;
		char chr;
		CommandVo commandVo = new CommandVo();
		while (index < length) {
			chr = context.charAt(index);
			if (chr == ' ' || chr == '\t' || chr == '\r' || chr == '\n') {// 过滤掉空白字符
				index++;
				continue;
			}
			if (chr >= 'a' && chr <= 'z' || chr >= 'A' && chr <= 'Z' || chr >= '0' && chr <= '9' || chr == '_') {// ActionName
				actionBuilder.append(chr);
				if (!findAction) {
					findAction = true;
				}
			} else if (chr == '.') {
				if (commandVoList.size() == 0 && (!findAction || actionBuilder.length() == 0)) {
					throw new TangYuanException("Invalid mongo command: " + context);
				}
				if (actionBuilder.length() > 0) {
					// add
					commandVo.setAction(actionBuilder.toString());
					commandVoList.add(commandVo);
					commandVo = new CommandVo();
					// clean
					actionBuilder.setLength(0);
					findAction = false;
				}
			} else if (chr == '(') {
				if (!findAction) {
					throw new TangYuanException("Invalid mongo command: " + context);
				}
				int endIndex = findMatchedChar(index, context, '(', ')');
				if (endIndex < 0) {
					throw new TangYuanException("Invalid mongo command: " + context);
				}

				commandVo.setAction(actionBuilder.toString());
				String parameter = context.substring(index + 1, endIndex);
				if (parameter.length() > 0) {
					List<String> parameters = this.parseParameter(parameter);
					commandVo.setParameters(parameters);
				}

				index = endIndex;
				commandVoList.add(commandVo);
				commandVo = new CommandVo();

				actionBuilder.setLength(0);
				findAction = false;
			} else {
				throw new TangYuanException("Invalid mongo command at position " + index + " for char '" + chr + "'");
			}
			index++;
		}

		if (commandVo.getAction() != null) {// Last Action
			commandVoList.add(commandVo);
		}

		return commandVoList;
	}

}
