#
# file  : stanza.rb
# brief : SNV Stanza file
# author: Satoshi Tanaka
#
# Copyright (c) 2016 DBCLS, S.Tanaka
# Released under the MIT license.
# https://github.com/DBCLS-human/multiomics15/blob/master/LICENSE


class SnvStanza < TogoStanza::Stanza::Base
  property :search do |sample, chromosome, range_start, range_end|
    "Sample=#{sample}, Chromosome=#{chromosome}, Range=#{range_start}-#{range_end}"
  end

  property :snvs do |sample, chromosome, range_start, range_end|
    query('http://humanrdf.dbcls.jp/sparql', <<-SPARQL.strip_heredoc)
        PREFIX rdf:              <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
        PREFIX rdfs:             <http://www.w3.org/2000/01/rdf-schema#>
        PREFIX owl:              <http://www.w3.org/2002/07/owl#>
        PREFIX obo:              <http://purl.obolibrary.org/obo/>
        PREFIX dc:               <http://purl.org/dc/terms/>
        PREFIX kero:             <http://dbtss.hgc.jp/rdf/ontology/>
        PREFIX faldo:            <http://biohackathon.org/resource/faldo#>
        PREFIX dbsnp:            <http://info.identifiers.org/dbsnp/>
        PREFIX ncbisnp:          <http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=>
        PREFIX ensemblvariation: <https://github.com/simonjupp/ensembl-rdf/blob/master/ontology/ensembl_variation_ontology.owl#>

        select distinct ?position replace( str( ?ref ), ".*#", "" ) as ?reference replace( str( ?alt ), ".*#", "" ) as ?alternative where {
            ?experiment kero:has_SNV ?sample;
                dc:identifier ?id;
        	rdfs:label ?label.
            ?sample a obo:SO_0001483;
                faldo:location ?location.
            ?location faldo:begin ?begin;
                faldo:reference ?chrom.
            ?begin faldo:position ?position.

            optional {
                ?sample ensemblvariation:has_allele ?ref.
                ?sample ensemblvariation:has_allele ?alt.
                ?ref a ensemblvariation:reference_allele.
                ?alt a ensemblvariation:derived_allele.
            }
       
            filter( ?id = "#{sample}" || ?label = "#{sample}" ).
            filter( replace( str( ?chrom ), ".*/", "" ) = "chromosome:GRCh38:#{chromosome}" ).
            filter( ?position >= #{range_start} ).
            filter( ?position <= #{range_end} ).
        }
    SPARQL
  end
end
