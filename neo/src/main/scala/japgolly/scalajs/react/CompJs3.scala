package japgolly.scalajs.react

import org.scalajs.dom
import scalajs.js
import japgolly.scalajs.react.internal._

object CompJs3 {
  type Constructor[P <: js.Object, S <: js.Object, C[a, b] <: CtorType[a, b]] = CompJs3X.Constructor[P, S, C, Mounted[P, S]]
  type Unmounted  [P <: js.Object, S <: js.Object] = CompJs3X.Unmounted  [P, S, Mounted[P, S]]
  type Mounted    [P <: js.Object, S <: js.Object] = CompJs3X.Mounted    [P, S, raw.ReactComponent]

  // TODO Change arg order to be consistent
  def Constructor[P <: js.Object, C <: ChildrenArg, S <: js.Object]
      (rc: raw.ReactClass)
      (implicit s: CtorType.Summoner[P, C]): Constructor[P, S, s.CC] =
    new CompJs3X.Constructor[P, S, s.CC, Mounted[P, S]](rc, s.pf.rmap(s.summon(rc))(Unmounted(_)))

  def Unmounted[P <: js.Object, S <: js.Object](r: raw.ReactComponentElement): Unmounted[P, S] =
    new CompJs3X.Unmounted(r, Mounted[P, S])

  def Mounted[P <: js.Object, S <: js.Object](r: raw.ReactComponent): Mounted[P, S] =
    CompJs3X.Mounted(r)
}

object CompJs3X {

  // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  class Constructor[P <: js.Object, S <: js.Object, C[a, b] <: CtorType[a, b], M](
        val rawCls: raw.ReactClass, val ctor: C[P, Unmounted[P, S, M]])
      extends BaseCtor[P, C, Unmounted[P, S, M]] {

    def mapMounted[MM](f: M => MM)(implicit p: Profunctor[C]): Constructor[P, S, C, MM] =
      new Constructor(rawCls, ctor rmap (_ mapMounted f))
  }

  class Unmounted[P <: js.Object, S <: js.Object, M](val rawElement: raw.ReactComponentElement, m: raw.ReactComponent => M) {

    def key: Option[Key] =
      orNullToOption(rawElement.key)

    def ref: Option[String] =
      orNullToOption(rawElement.ref)

    def props: P =
      rawElement.props.asInstanceOf[P]

    def propsChildren: PropsChildren =
      PropsChildren(rawElement.props.children)

    def mapMounted[MM](f: M => MM): Unmounted[P, S, MM] =
      new Unmounted(rawElement, f compose m)

    def renderIntoDOM(container: raw.ReactDOM.Container, callback: Callback = Callback.empty): M =
      m(raw.ReactDOM.render(rawElement, container, callback.toJsFn))
  }

  def Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent](r: Raw): Mounted[P, S, Raw] =
    new Mounted[P, S, Raw] {
      override val rawInstance = r
    }

  trait Mounted[P <: js.Object, S <: js.Object, Raw <: raw.ReactComponent]
      extends MountedBase[Effect.Id, P, S] {

    val rawInstance: Raw

    override protected final implicit def F = Effect.InstanceId

    def addRawType[T <: js.Object]: Mounted[P, S, Raw with T] =
      this.asInstanceOf[Mounted[P, S, Raw with T]]

    //      def getDefaultProps: Props
    //      def getInitialState: js.Object | Null
    //      def render(): ReactElement

    override final def isMounted =
      rawInstance.isMounted()

    override final def props: P =
      rawInstance.props.asInstanceOf[P]

    override final def propsChildren =
      PropsChildren(rawInstance.props.children)

    override final def state: S =
      rawInstance.state.asInstanceOf[S]

    override final def setState(state: S, callback: Callback = Callback.empty): Unit =
      rawInstance.setState(state, callback.toJsFn)

    override final def modState(mod: S => S, callback: Callback = Callback.empty): Unit =
      rawInstance.modState(mod.asInstanceOf[js.Object => js.Object], callback.toJsFn)

    override final def getDOMNode: dom.Element =
      raw.ReactDOM.findDOMNode(rawInstance)

    override final def forceUpdate(callback: Callback = Callback.empty): Unit =
      rawInstance.forceUpdate(callback.toJsFn)

//    override final def mapProps[A](f: P => A): Mounted[A, S, Raw] = {
//      val self = this
//      new Mounted[A, S, Raw] {
//        override val rawInstance = self.rawInstance
//        override def props: A = f(self.props)
//      }
//    }
  }

}