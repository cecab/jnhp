# Jnhp, A tiny template tool 
Jnhp is a template solution for servlet applications, it features a tiny API to manage the creation of dynamic HTML content from java code, avoiding the use of additional languages like JSP 

The purpose of this blog is explains the organization of the logic and design that is behind the implementation. This includes: the parsing to create a graph (a Tree to be more precise), the implementation of the API, which translate the API into mutations in the graph, and the integration with Servlet API so we can build web applications. 

## A Brief Idea of our API

