package org.eclipse.cdt.internal.core.dom;


import org.eclipse.cdt.core.dom.IScope;
import org.eclipse.cdt.internal.core.newparser.IParserCallback;
import org.eclipse.cdt.internal.core.newparser.Token;

/**
 * This is the parser callback that creates objects in the DOM.
 */
public class DOMBuilder implements IParserCallback 
{

	private TranslationUnit translationUnit;
	
	public TranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsBegin()
	 */
	public Object argumentsBegin( Object declarator ) {
		Declarator decl = ((Declarator)declarator);
		ParameterDeclarationClause clause = new ParameterDeclarationClause( decl ); 
		return clause; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#argumentsEnd()
	 */
	public void argumentsEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classBegin(java.lang.String, org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object classSpecifierBegin(Object container, Token classKey) {
		SimpleDeclaration decl = (SimpleDeclaration)container;
		
		int kind = ClassSpecifier.t_struct;
		
		switch (classKey.getType()) {
			case Token.t_class:
				kind = ClassSpecifier.t_class;
				break;
			case Token.t_struct:
				kind = ClassSpecifier.t_struct;
				break;
			case Token.t_union:
				kind = ClassSpecifier.t_union;
				break;			
		}
		
		ClassSpecifier classSpecifier = new ClassSpecifier(kind, decl);
		decl.setTypeSpecifier(classSpecifier);
		return classSpecifier;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classSpecifierName()
	 */
	public void classSpecifierName(Object classSpecifier) {
		((ClassSpecifier)classSpecifier).setName(currName);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#classEnd()
	 */
	public void classSpecifierEnd(Object classSpecifier) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorBegin()
	 */
	public Object declaratorBegin(Object container) {
		DeclarationSpecifier.Container decl = (DeclarationSpecifier.Container )container; 
		Declarator declarator = new Declarator(decl);
		decl.addDeclarator(declarator);
		return declarator; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorEnd()
	 */
	public void declaratorEnd(Object declarator) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declaratorId(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void declaratorId(Object declarator) {
		((Declarator)declarator).setName(currName);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#declSpecifier(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclSpecifier(Object Container, Token specifier) {
		DeclarationSpecifier.Container decl = (DeclarationSpecifier.Container)Container;
		DeclarationSpecifier declSpec = decl.getDeclSpecifier(); 
		if( declSpec == null )
		{
			declSpec = new DeclarationSpecifier(); 
			decl.setDeclSpecifier( declSpec ); 
		}
		
		switch (specifier.getType()) {
			case Token.t_auto:
				declSpec.setAuto(true);
				break;
			case Token.t_register:
				declSpec.setRegister(true);
				break;
			case Token.t_static:
				declSpec.setStatic(true);
				break;
			case Token.t_extern:
				declSpec.setExtern(true);
				break;
			case Token.t_mutable:
				declSpec.setMutable(true);
				break;
			case Token.t_inline:
				declSpec.setInline(true);
				break;
			case Token.t_virtual:
				declSpec.setVirtual(true);
				break;
			case Token.t_explicit:
				declSpec.setExplicit(true);
				break;
			case Token.t_typedef:
				declSpec.setTypedef(true);
				break;
			case Token.t_friend:
				declSpec.setFriend(true);
				break;
			case Token.t_const:
				declSpec.setConst(true);
				break;
			case Token.t_volatile:
				declSpec.setVolatile(true);
				break;
			case Token.t_char:
				declSpec.setType(DeclarationSpecifier.t_char);
				break;
			case Token.t_wchar_t:
				declSpec.setType(DeclarationSpecifier.t_wchar_t);
				break;
			case Token.t_bool:
				declSpec.setType(DeclarationSpecifier.t_bool);
				break;
			case Token.t_short:
				declSpec.setShort(true);
				break;
			case Token.t_int:
				declSpec.setType(DeclarationSpecifier.t_int);
				break;
			case Token.t_long:
			declSpec.setLong(true);
				break;
			case Token.t_signed:
			declSpec.setUnsigned(false);
				break;
			case Token.t_unsigned:
			declSpec.setUnsigned(true);
				break;
			case Token.t_float:
			declSpec.setType(DeclarationSpecifier.t_float);
				break;
			case Token.t_double:
			declSpec.setType(DeclarationSpecifier.t_double);
				break;
			case Token.t_void:
			declSpec.setType(DeclarationSpecifier.t_void);
				break;
		}
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionOperator(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionOperator(Token operator) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#expressionTerminal(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void expressionTerminal(Token terminal) throws Exception {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyBegin()
	 */
	public void functionBodyBegin() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#functionBodyEnd()
	 */
	public void functionBodyEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionBegin(java.lang.String)
	 */
	public void inclusionBegin(String includeFile) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#inclusionEnd()
	 */
	public void inclusionEnd() {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#macro(java.lang.String)
	 */
	public void macro(String macroName) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public Object simpleDeclarationBegin(Object container, Token firstToken) {
		SimpleDeclaration decl = new SimpleDeclaration();
		((IScope)container).addDeclaration(decl);
		return decl;
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#simpleDeclarationEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void simpleDeclarationEnd(Object declaration) {
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitBegin()
	 */
	public Object translationUnitBegin() {
		translationUnit = new TranslationUnit();
		return translationUnit; 
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#translationUnitEnd()
	 */
	public void translationUnitEnd(Object unit) {
	}

	private Name currName;
	
	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameBegin(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameBegin(Token firstToken) {
		currName = new Name(firstToken);
	}

	/**
	 * @see org.eclipse.cdt.internal.core.newparser.IParserCallback#nameEnd(org.eclipse.cdt.internal.core.newparser.Token)
	 */
	public void nameEnd(Token lastToken) {
		currName.setEnd(lastToken);
	}

	public Object baseSpecifierBegin( Object classSpecifier )
	{
		ClassSpecifier cs =(ClassSpecifier)classSpecifier;
		BaseSpecifier baseSpec = new BaseSpecifier( cs );
		return baseSpec; 
	}

	public void baseSpecifierEnd( Object baseSpecifier  )
	{
		
	}

	public void baseSpecifierVirtual( Object baseSpecifier, boolean virtual )
	{
		BaseSpecifier bs = (BaseSpecifier)baseSpecifier; 
		bs.setVirtual( virtual );
	}

	public void baseSpecifierVisibility( Object baseSpecifier, Token visibility )
	{
		int access = BaseSpecifier.t_public;  
		switch( visibility.type )
		{
		case Token.t_public:
			access = BaseSpecifier.t_public; 
			break; 
		case Token.t_protected:
			access = BaseSpecifier.t_protected;		 
			break;
		case Token.t_private:
			access = BaseSpecifier.t_private;
			break; 		
		default: 
			break;
		}
	
		((BaseSpecifier)baseSpecifier).setAccess(access);  
	}

	
	public void baseSpecifierName( Object baseSpecifier )
	{
		((BaseSpecifier)baseSpecifier).setName(currName.getName());		
	}
	
	public Object parameterDeclarationBegin( Object container, Token firstToken )
	{
		ParameterDeclarationClause clause = (ParameterDeclarationClause)container; 
		ParameterDeclaration pd = new ParameterDeclaration();
		clause.addDeclaration( pd ); 
		return pd;
	}
	
	public void  parameterDeclarationEnd( Object declaration ){
	}
}