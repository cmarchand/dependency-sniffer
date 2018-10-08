module namespace deps = 'top:marchand:maven:dependency:explorer';

(: Matches all other all paths starting with "deps/browse/..." :)
declare %rest:path("deps/browse/{$path=.+}")
  function deps:browse($path as xs:string?) {
     let $tree := collection('DEPS')/tree 
     let $root := deps:getRootNode($tree, tokenize($path,'/'))
     let $serial := (for $n in $root/* 
      return concat(
        '{', 
        string-join(
          for $attr in $n/@* return 
            concat(
              '"',
              local-name($attr),
              '": "', 
              string($attr), 
              '"'
            ), 
            ','
          ),
        '}'
      ))
      
     return (
      <rest:response>
       <output:serialization-parameters>
        <output:media-type value='text/plain'/>
       </output:serialization-parameters>
      </rest:response>,
      concat('[', string-join($serial,','), ']')
     )
  };
(: Matches all other all paths starting with "deps/browse/..." :)
declare %rest:path("deps/browse")
  %rest:produces("application/json")
  function deps:browse() {
     let $root := collection('DEPS')/tree 
     let $serial := (for $n in $root/* 
      return concat(
        '{', 
        string-join(
          for $attr in $n/@* return 
            concat(
              '"',
              local-name($attr),
              '": "', 
              string($attr), 
              '"'
            ), 
            ','
          ),
        '}'
      ))
      
     return (
      <rest:response>
       <output:serialization-parameters>
        <output:media-type value='text/plain'/>
       </output:serialization-parameters>
      </rest:response>,
      concat('[', string-join($serial,','), ']')
     )
  };
  
declare %rest:path("deps/artifact/{$dep=.+}")
  function deps:showDep($dep as xs:string) {
    let $artifact as element(artifact) := (db:attribute('DEPS', $dep, 'id')/../self::artifact)[1] 
    return (
      <rest:response>
       <output:serialization-parameters>
        <output:media-type value='text/plain'/>
       </output:serialization-parameters>
      </rest:response>,
      deps:artifactToJSon($artifact)
    )
  };

declare %rest:path("deps/reverse/{$id=.+}")
  function deps:showReverse($id as xs:string) {
    concat('[', string-join(for $artifact in db:attribute('DEPS', $id, 'id')/../dependency return deps:dependencyToJSon($artifact), ','), ']') 
  };

declare function deps:artifactToJSon($artifact as element(artifact)) as xs:string {
  concat('{
  "id":"', $artifact/@id,'",
  ', deps:designationToJSon($artifact/designation), '
  ,', deps:dependenciesToJSon($artifact/dependency), '
  ,', deps:treeToJSon($artifact/tree), '
}')
};

declare function deps:designationToJSon($designation as element(designation)) as xs:string {
  concat('"designation": {
    "groupId":"', $designation/groupId/text(), '",
    "artifactId":"', $designation/artifactId/text(), '",
    "version":"', $designation/version/text(), '" }
    ')
};

declare function deps:dependenciesToJSon($deps as element(dependency)*) as xs:string {
  let $dependencies := for $dep in $deps return deps:dependencyToJSon($dep)
  return concat('"dependencies": [', string-join($dependencies, ','), ']')
};

declare function deps:dependencyToJSon($dep as element(dependency)) as xs:string {
  concat('{
      "groupId":"', $dep/groupId/text(), '",
      "artifactId":"', $dep/artifactId/text(), '",
      "version":"', $dep/version/text(), '",
      "id":"', $dep/@id, '"
    }')
};

declare function deps:treeToJSon($tree as element(tree)) as xs:string {
  concat('"tree":"', replace(replace($tree/text(), '\\', '\\\\'), '
', '\\n'), '"')
};

declare function deps:getRootNode($root as element(*)?, $path as xs:string*) as element(*)? {
  if(empty($path)) then $root
  else deps:getRootNode($root/node[@name eq $path[1]], $path[position() gt 1] )
};