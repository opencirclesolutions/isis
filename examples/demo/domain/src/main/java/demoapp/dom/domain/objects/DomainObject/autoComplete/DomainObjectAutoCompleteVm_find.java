package demoapp.dom.domain.objects.DomainObject.autoComplete;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;

//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class DomainObjectAutoCompleteVm_find {

    @SuppressWarnings("unused")
    private final DomainObjectAutoCompleteVm mixee;

    @MemberSupport
    public DomainObjectAutoComplete act(final DomainObjectAutoComplete domainObjectAutoComplete) {  // <.>
        return domainObjectAutoComplete;
    }

}
//end::class[]
