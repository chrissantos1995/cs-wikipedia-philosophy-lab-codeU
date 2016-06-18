package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	static Set<String> urls;
	static Integer parenthesesCheck;
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		parenthesesCheck = 0;
		
        urls = new HashSet<>();

		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";

		urls.add(url);

		System.out.println(findPhilosophyPage(url));
	}

	private static boolean findPhilosophyPage(String url) throws IOException{

		// Search for first link

		Elements paragraphs = wf.fetchWikipedia(url);
		Element currEl;
		String firstValidLink;
		Iterable<Node> iter;

		for(Element el: paragraphs) {

			firstValidLink = findFirstValidLink(el);

			if(firstValidLink == null)
				return false;

			if(isPhilosophyLink(firstValidLink))
				return true;

			return findPhilosophyPage(firstValidLink);
		}

		return false;
	}

	private static String findFirstValidLink(Element el) {

		Iterable<Node> iter = new WikiNodeIterable(el);
		
		Element currEl;

		for(Node node: iter) {

			if(node instanceof Element) {

				currEl = (Element) node;

				if(isValidAnchor(currEl)) 
					return currEl.absUrl("href");
			}

			if(node instanceof TextNode)
				checkParentheses((TextNode) node);
		}

		return null;
	}

	private static boolean isPhilosophyLink(String url) {
		return url.contains("/wiki/Wikipedia:Getting_to_Philosophy");
	}

	private static boolean isValidAnchor(Element el) {
		return isAnchor(el) && 
			   hasUniqueLink(el) &&
		       !insideParentheses() && 
		       !isItalicized(el) &&
		       !isRedText(el) &&
		       !isExternalLink(el);
	}

	private static boolean isAnchor(Element el) {
		return el.tagName().equals("a");
	}

	private static boolean hasUniqueLink(Element el) {
		return urls.add(el.absUrl("href"));
	}

	private static boolean insideParentheses() {
		return parenthesesCheck > 0;
	}

	private static boolean isItalicized(Element el) {

		Elements parentsList = el.parents();

		for(Element currEl: parentsList) {
			if(currEl.tagName().equals("i"))
				return true;
		}

		return false;
	}

	private static boolean isRedText(Element el) {
		return el.hasClass("new");
	}

	private static boolean isExternalLink(Element el) {
		return !el.absUrl("href").contains("wikipedia.org");
	}

	private static void checkParentheses(TextNode textNode) {

		for(char c: textNode.text().toCharArray()) {
			if(c == '(')
				parenthesesCheck++;
			else if(c == ')')
				parenthesesCheck--;
		}
	}
}
