package org.codehaus.groovy.grails.web.sitemesh

import com.opensymphony.module.sitemesh.RequestConstants

import org.codehaus.groovy.grails.web.taglib.AbstractGrailsTagTests
import org.codehaus.groovy.grails.web.pages.FastStringWriter
import org.springframework.web.context.request.RequestContextHolder

class GSPSitemeshPageTests extends AbstractGrailsTagTests {

    void testCaptureContent() {
        def template='<sitemesh:captureContent tag=\"testtag\">this is the captured content</sitemesh:captureContent>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals 'this is the captured content', gspSiteMeshPage.getContentBuffer('page.testtag').toString()
    }

    void testCaptureContent2() {
        def template='<sitemesh:captureContent tag=\"testtag\">this is the <g:if test="${true}">captured</g:if> content</sitemesh:captureContent>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals 'this is the captured content', gspSiteMeshPage.getContentBuffer('page.testtag').toString()
    }

    void testCaptureContent3() {
        def template='<content tag=\"testtag\">this is the <g:if test="${true}">captured</g:if> content</content>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals 'this is the captured content', gspSiteMeshPage.getContentBuffer('page.testtag').toString()
    }

    void testCaptureTitleAndBody() {
        def template='<html><head><title>This is the title</title></head><body onload="somejs();">body here</body></html>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals 'This is the title', gspSiteMeshPage.getProperty('title')
        FastStringWriter writer=new FastStringWriter()
        gspSiteMeshPage.writeBody(writer)
        assertEquals 'body here', writer.toString()
        assertEquals 'somejs();', gspSiteMeshPage.getProperty('body.onload')
    }

    void testMetaObjectValues() {
        // GRAILS-5603 test case
        def template='<html><head><meta name="intval" content="${123}"/><meta name="dateval" content="${new Date(0)}"/><title>This is the title</title></head><body onload="somejs();">body here</body></html>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals '123', gspSiteMeshPage.getProperty('meta.intval')
        assertEquals new Date(0).toString(), gspSiteMeshPage.getProperty('meta.dateval')
    }

    void testLayoutTags() {
        def template='<html><head><title>This is the title</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></head><body onload="somejs();">body here</body></html>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals 'This is the title', gspSiteMeshPage.getProperty('title')

        def gspSiteMeshPage2 = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage2)
        webRequest.currentRequest.setAttribute(RequestConstants.PAGE, gspSiteMeshPage)
        def template2='<html><head><title><g:layoutTitle/></title><g:layoutHead/></head><body onload=\"${pageProperty(name:\'body.onload\')}\"><g:layoutBody/></body></html>'
        def result2 = applyTemplate(template2, [:])

        assertEquals '<html><head><title>This is the title</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></head><body onload="somejs();">body here</body></html>', result2
    }

    void testLayoutTagsBodyIsWholePage() {
        def template='body here'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def target1 = new FastStringWriter()
        gspSiteMeshPage.setPageBuffer(target1.buffer)
        def result = applyTemplate(template, [:], target1)

        def gspSiteMeshPage2 = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage2)
        webRequest.currentRequest.setAttribute(RequestConstants.PAGE, gspSiteMeshPage)
        def target2 = new FastStringWriter()
        gspSiteMeshPage2.setPageBuffer(target2.buffer)
        def template2='<body><g:layoutBody/></body>'
        def result2 = applyTemplate(template2, [:], target2)

        assertEquals '<body>body here</body>', result2
    }

    void testLayoutcontent() {
        def template='<html><head><title>This is the title</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body onload="somejs();">body here</body><content tag="nav">Navigation content</content></html>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals 'This is the title', gspSiteMeshPage.getProperty('title')

        def gspSiteMeshPage2 = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage2)
        webRequest.currentRequest.setAttribute(RequestConstants.PAGE, gspSiteMeshPage)
        def template2='<html><head><title><g:layoutTitle/></title><g:layoutHead/></head><body onload=\"${pageProperty(name:\'body.onload\')}\"><g:layoutBody/> <g:pageProperty name="page.nav"/></body></html>'
        def result2 = applyTemplate(template2, [:])

        assertEquals '<html><head><title>This is the title</title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body onload="somejs();">body here Navigation content</body></html>', result2
    }

    void testEmptyTitle() {
        // GRAILS-7510 , GRAILS-7736
        def template='<html><head><title></title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body onload="somejs();">body here</body><content tag="nav">Navigation content</content></html>'
        def gspSiteMeshPage = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage)
        def result = applyTemplate(template, [:])
        assertEquals '', gspSiteMeshPage.getProperty('title')

        def gspSiteMeshPage2 = new GSPSitemeshPage()
        webRequest.currentRequest.setAttribute(GrailsLayoutView.GSP_SITEMESH_PAGE, gspSiteMeshPage2)
        webRequest.currentRequest.setAttribute(RequestConstants.PAGE, gspSiteMeshPage)
        def template2='<html><head><title><g:layoutTitle/></title><g:layoutHead/></head><body onload=\"${pageProperty(name:\'body.onload\')}\"><g:layoutBody/> <g:pageProperty name="page.nav"/></body></html>'
        def result2 = applyTemplate(template2, [:])

        assertEquals '<html><head><title></title><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/></head><body onload="somejs();">body here Navigation content</body></html>', result2
    }

    void tearDown() {
         RequestContextHolder.setRequestAttributes(null)
    }
}
